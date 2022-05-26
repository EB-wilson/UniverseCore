package universecore.world.consumers;

import arc.func.*;
import arc.graphics.g2d.TextureRegion;
import arc.scene.event.Touchable;
import arc.struct.ObjectMap;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;
import universecore.util.UncLiquidStack;

/**消耗列表，记录一个消耗的包含生产时间，可选等在内的所有信息
 * @author EBwilson */
@SuppressWarnings("unchecked")
public class BaseConsumers{
  protected final ObjectMap<UncConsumeType<?>, BaseConsume<?>> cons = new ObjectMap<>();
  
  /**该值控制生产消耗的时间*/
  public float craftTime = 60;
  /**是否在统计栏显示消耗所需时间*/
  public boolean showTime = false;
  
  /**是否为可选*/
  public final boolean optional;
  public boolean optionalAlwaysValid = true;

  public Floatf<ConsumerBuildComp> consDelta;
  
  /**图标，在选择消耗列表时显示，默认为首个消耗项*/
  public Prov<TextureRegion> icon;
  /**可选列表可用时将随更新执行的目标函数*/
  public Cons2<ConsumerBuildComp, BaseConsumers> optionalDef = (entity, cons) -> {};
  /**在统计信息显示自定义内容的函数*/
  public Cons2<Stats, BaseConsumers> display = (stats, cons) -> {};

  public Prov<Visibility> selectable = () -> Visibility.usable;
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

  public <N extends ConsumerBuildComp> void setDelta(Floatf<N> delta){
    this.consDelta = (Floatf<ConsumerBuildComp>) delta;
  }

  public float delta(ConsumerBuildComp entity){
    return consDelta == null? entity.getBuilding().edelta(): consDelta.get(entity);
  }
  
  public UncConsumeItems<? extends ConsumerBuildComp> item(Item item, int amount){
    return items(new ItemStack[]{new ItemStack(item, amount)});
  }
  
  public UncConsumeItems<? extends ConsumerBuildComp> items(ItemStack[] items){
    return add(new UncConsumeItems<>(items));
  }
  
  public UncConsumeLiquids<? extends ConsumerBuildComp> liquid(Liquid liquid, float amount){
    return liquids(new UncLiquidStack[]{new UncLiquidStack(liquid, amount)});
  }
  
  public UncConsumeLiquids<? extends ConsumerBuildComp> liquids(UncLiquidStack[] liquids){
    return add(new UncConsumeLiquids<>(liquids));
  }
  
  public UncConsumePower<? extends ConsumerBuildComp> power(float usage){
    return add(new UncConsumePower<>(usage, false));
  }
  
  @SuppressWarnings("rawtypes")
  public UncConsumePower<? extends ConsumerBuildComp> powerCond(float usage, Boolf<Building> cons){
    return add(new UncConsumePower(usage, false){
      private final Boolf<Building> consume = cons;
      public float requestedPower(Building entity){
        return consume.get(entity) ? usage : 0f;
      }
    });
  }

  public <T extends BaseConsume<? extends ConsumerBuildComp>> T add(T consume){
    cons.put(consume.type(), consume);
    consume.parent = this;
    if(icon == null && consume.icon() != BaseConsume.EMPTY_TEX){
      icon = consume::icon;
    }
    return consume;
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseConsume<? extends ConsumerBuildComp>> T get(UncConsumeType<T> type){
    return (T) cons.get(type);
  }

  public Iterable<BaseConsume<? extends ConsumerBuildComp>> all(){
    return cons.values();
  }

  public void remove(UncConsumeType<?> type){
    cons.remove(type);
  }

  public void display(Stats stats){
    if(cons.size > 0){
      if(showTime) stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
      for(BaseConsume<?> c: cons.values().toSeq().sort((a, b) -> a.type().id() - b.type().id())){
        if(c == null) return;
        c.display(stats);
      }
      display.get(stats, this);
    }
  }

  public enum Visibility{
    usable(Touchable.enabled),
    unusable(Touchable.disabled),
    hidden(Touchable.disabled);

    public final Touchable buttonValid;

    Visibility(Touchable touchable){
      buttonValid = touchable;
    }
  }
}
