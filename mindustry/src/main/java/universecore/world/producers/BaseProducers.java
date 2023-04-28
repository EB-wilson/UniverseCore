package universecore.world.producers;

import arc.graphics.Color;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.meta.Stats;
import universecore.world.consumers.BaseConsumers;

/**产出列表，绑定一个消耗列表，在执行消耗的同时对应执行此生产列表，以实现工厂生产
 * @author EBwilson */
public class BaseProducers{
  final static Color TRANS = new Color(0, 0, 0, 0);
  protected static final Seq<BaseProduce<?>> tmpProd = new Seq<>();

  protected final ObjectMap<ProduceType<?>, BaseProduce<?>> prod = new ObjectMap<>();

  /**用于显示选择配方的顶部颜色*/
  public Color color = TRANS;
  
  public BaseConsumers cons;

  public BaseProducers setColor(Color color){
    this.color = color;
    return this;
  }
  
  public ProduceItems<?> item(Item item, int amount){
    return items(new ItemStack(item, amount));
  }
  
  public ProduceItems<?> items(ItemStack... items){
    return add(new ProduceItems<>(items));
  }
  
  public ProduceLiquids<?> liquid(Liquid liquid, float amount){
    return liquids(new LiquidStack(liquid, amount));
  }
  
  public ProduceLiquids<?> liquids(LiquidStack... liquids){
    return add(new ProduceLiquids<>(liquids));
  }
  
  public ProducePower<?> power(float prod){
    return add(new ProducePower<>(prod));
  }
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T extends BaseProduce<?>> T add(T produce){
    BaseProduce p = prod.get(produce.type());
    if(p == null){
      prod.put(produce.type(), produce);
      produce.parent = this;
      if(color == TRANS && produce.color() != null){
        color = produce.color();
      }
      return produce;
    }
    else p.merge(produce);
    return (T) p;
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseProduce<?>> T get(ProduceType<T> type){
    return (T) prod.get(type);
  }

  public Iterable<BaseProduce<?>> all(){
    tmpProd.clear();

    for (ProduceType<?> type : ProduceType.all()) {
      BaseProduce<?> p = prod.get(type);
      if (p != null) tmpProd.add(p);
    }

    return tmpProd;
  }

  public void remove(ProduceType<?> type){
    prod.remove(type);
  }

  public void display(Stats stats){
    if(prod.size > 0){
      for(BaseProduce<?> p: prod.values().toSeq().sort((a, b) -> a.type().id() - b.type().id())){
        p.display(stats);
      }
    }
  }
}
