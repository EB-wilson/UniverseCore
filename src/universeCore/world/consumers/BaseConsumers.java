package universeCore.world.consumers;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Cons2;
import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ConsumerBuildComp;
import universeCore.util.UncLiquidStack;

import java.util.HashMap;

public class BaseConsumers{
  protected final HashMap<UncConsumeType<?>, BaseConsume<?>> cons = new HashMap<>();
  
  /**在存在物品消耗时，此值为0将在初始化时设置为90
   * 该值控制生产消耗的时间*/
  public float craftTime = 0;
  
  public final boolean optional;
  
  public TextureRegion icon;
  public Cons2<ConsumerBuildComp, BaseConsumers> optionalDef = (entity, cons) -> {};
  public Cons2<Stats, BaseConsumers> display = (stats, cons) -> {};
  
  public Boolf<ConsumerBuildComp> valid = e -> true;
  public Cons<ConsumerBuildComp> trigger = e -> {};
  
  public BaseConsumers(boolean optional){
    this.optional = optional;
  }

  public BaseConsumers setIcon(TextureRegion icon){
    this.icon = icon;
    return this;
  }
  
  public void time(float time){
    this.craftTime = time;
  }
  
  public UncConsumeItems item(Item item, int amount){
    return items(new ItemStack[]{new ItemStack(item, amount)});
  }
  
  public UncConsumeItems items(ItemStack[] items){
    return add(new UncConsumeItems(items));
  }
  
  public UncConsumeLiquids liquid(Liquid liquid, float amount){
    return liquids(new UncLiquidStack[]{new UncLiquidStack(liquid, amount)});
  }
  
  public UncConsumeLiquids liquids(UncLiquidStack[] liquids){
    return add(new UncConsumeLiquids(liquids));
  }
  
  public UncConsumePower power(float usage){
    return add(new UncConsumePower(usage, false));
  }
  
  public UncConsumePower powerCond(float usage, Boolf<Building> cons){
    return add(new UncConsumePower(usage, false){
      private final Boolf<Building> consume = cons;
      public float requestedPower(Building entity){
        return consume.get(entity) ? usage : 0f;
      }
    });
  }

  public <T extends BaseConsume<?>> T add(T consume){
    cons.put(consume.type(), consume);
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
      if(craftTime > 0) stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
      cons.forEach((k, c) -> {
        if(c == null) return;
        c.display(stats);
      });
      display.get(stats, this);
    }
  }
}
