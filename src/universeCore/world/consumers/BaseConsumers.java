package universeCore.world.consumers;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Cons2;
import arc.func.Prov;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ConsumerBuildComp;
import universeCore.util.UncLiquidStack;
import universeCore.world.producers.BaseProduce;

import java.util.Arrays;
import java.util.HashMap;

/**消耗列表，记录一个消耗的包含生产时间，可选性等在内的所有信息
 * @author EBwilson */
public class BaseConsumers{
  protected final HashMap<UncConsumeType<?>, BaseConsume<?>> cons = new HashMap<>();
  
  /**该值控制生产消耗的时间*/
  public float craftTime = 60;
  /**是否在统计栏显示消耗所需时间*/
  public boolean showTime = false;
  
  /**是否为可选*/
  public final boolean optional;
  /**是否接受超速加成*/
  public boolean acceptOverdrive = true;
  
  /**图标，在选择消耗列表时显示，默认为首个消耗项*/
  public Prov<TextureRegion> icon;
  /**可选列表可用时将随更新执行的目标函数*/
  public Cons2<ConsumerBuildComp, BaseConsumers> optionalDef = (entity, cons) -> {};
  /**在统计信息显示自定义内容的函数*/
  public Cons2<Stats, BaseConsumers> display = (stats, cons) -> {};
  
  /**本消耗的可用控制器*/
  public Boolf<ConsumerBuildComp> valid = e -> true;
  /**消耗触发器，在消耗的trigger()方法执行时触发*/
  public Cons<ConsumerBuildComp> trigger = e -> {};
  
  public BaseConsumers(boolean optional){
    this.optional = optional;
  }
  
  public BaseConsumers setIcon(TextureRegion icon){
    this.icon = () -> icon;
    return this;
  }
  
  public void time(float time){
    this.craftTime = time;
    showTime = true;
  }
  
  public UncConsumeItems<?> item(Item item, int amount){
    return items(new ItemStack[]{new ItemStack(item, amount)});
  }
  
  public UncConsumeItems<?> items(ItemStack[] items){
    return add(new UncConsumeItems<>(items));
  }
  
  public UncConsumeLiquids<?> liquid(Liquid liquid, float amount){
    return liquids(new UncLiquidStack[]{new UncLiquidStack(liquid, amount)});
  }
  
  public UncConsumeLiquids<?> liquids(UncLiquidStack[] liquids){
    return add(new UncConsumeLiquids<>(liquids));
  }
  
  public UncConsumePower<?> power(float usage){
    return add(new UncConsumePower<>(usage, false));
  }
  
  @SuppressWarnings("rawtypes")
  public UncConsumePower<?> powerCond(float usage, Boolf<Building> cons){
    return add(new UncConsumePower(usage, false){
      private final Boolf<Building> consume = cons;
      public float requestedPower(Building entity){
        return consume.get(entity) ? usage : 0f;
      }
    });
  }

  public <T extends BaseConsume<?>> T add(T consume){
    cons.put(consume.type(), consume);
    consume.parent = this;
    if(icon == null && consume.icon() != BaseConsume.EMPTY_TEX){
      icon = consume::icon;
    }
    return consume;
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseConsume<?>> T get(UncConsumeType<T> type){
    return (T) cons.get(type);
  }

  public BaseConsume<?>[] all(){
    return cons.values().toArray(new BaseConsume[0]);
  }

  public void remove(UncConsumeType<?> type){
    cons.remove(type);
  }

  public void display(Stats stats){
    if(cons.size() > 0){
      if(showTime) stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
      BaseConsume<?>[] arr = cons.values().toArray(new BaseConsume<?>[0]);
      Arrays.sort(arr, (a, b) -> a.type().id() - b.type().id());
      for(BaseConsume<?> c: arr){
        if(c == null) return;
        c.display(stats);
      }
      display.get(stats, this);
    }
  }
}
