package universeCore.world.consumers;

import arc.func.Boolf;
import arc.func.Cons2;
import arc.graphics.g2d.TextureRegion;
import arc.util.Structs;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universeCore.util.UncLiquidStack;

import java.util.ArrayList;
import java.util.Objects;

public class BaseConsumers{
  protected final ArrayList<BaseConsume> cons = new ArrayList<>(UncConsumeType.all().length);
  
  /**在存在物品消耗时，此值为0将在初始化时设置为90
   * 该值控制生产消耗的时间*/
  public float craftTime = 0;
  
  public final boolean optional;
  
  public TextureRegion icon;
  public Cons2<Building, BaseConsumers> method = (entity, cons) -> {};
  public Cons2<Stats, BaseConsumers> display = (stats, cons) -> {};
  
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

  public <T extends BaseConsume> T add(T consume){
    if(consume.type().id() >= cons.size()){
      int temp = consume.type().id() - cons.size() + 1;
      for(int i=0; i<temp; i++){
        cons.add(null);
      }
    }
    cons.set(consume.type().id(), consume);
    return consume;
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseConsume> T get(UncConsumeType<T, ?> type){
    return type.id() >= 0 && type.id() < cons.size()? (T)cons.get(type.id()): null;
  }

  public BaseConsume[] all(){
    return Structs.filter(BaseConsume.class, cons.toArray(new BaseConsume[0]), Objects::nonNull);
  }

  public void remove(int id){
    cons.set(id, null);
  }

  public void display(Stats stats){
    if(cons.size() > 0){
      if(craftTime > 0) stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
      for(BaseConsume c: cons){
        if(c == null) continue;
        c.display(stats);
      }
      display.get(stats, this);
    }
  }
}
