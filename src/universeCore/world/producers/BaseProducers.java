package universeCore.world.producers;

import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;
import arc.util.Structs;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.world.meta.Stats;
import universeCore.util.*;
import universeCore.world.consumers.BaseConsumers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/**产出列表，绑定一个消耗列表，在执行消耗的同时对应执行此生产列表，以实现工厂生产
 * @author EBwilson */
public class BaseProducers{
  protected final HashMap<ProduceType<?>, BaseProduce<?>> prod = new HashMap<>();

  /**用于显示选择配方的图标*/
  public Prov<TextureRegion> icon;
  /**用于显示选择配方的顶部颜色*/
  public Color color;
  
  public BaseConsumers cons;

  public BaseProducers setIcon(TextureRegion icon){
    this.icon = () -> icon;
    return this;
  }

  public BaseProducers setColor(Color color){
    this.color = color;
    return this;
  }
  
  public ProduceItems<?> item(Item item, int amount){
    return items(new ItemStack[]{new ItemStack(item, amount)});
  }
  
  public ProduceItems<?> items(ItemStack[] items){
    return add(new ProduceItems<>(items));
  }
  
  public ProduceLiquids<?> liquid(Liquid liquid, float amount){
    return liquids(new UncLiquidStack[]{new UncLiquidStack(liquid, amount)});
  }
  
  public ProduceLiquids<?> liquids(UncLiquidStack[] liquids){
    return add(new ProduceLiquids<>(liquids));
  }
  
  public ProducePower<?> power(float prod){
    return add(new ProducePower<>(prod));
  }
  
  public <T extends BaseProduce<?>> T add(T produce){
    prod.put(produce.type(), produce);
    produce.parent = this;
    if(icon == null && produce.icon() != BaseProduce.EMPTY_TEX){
      icon = produce::icon;
    }
    return produce;
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseProduce<?>> T get(ProduceType<T> type){
    return (T) prod.get(type);
  }

  public BaseProduce<?>[] all(){
    return prod.values().toArray(new BaseProduce[0]);
  }

  public void remove(ProduceType<?> type){
    prod.remove(type);
  }

  public void display(Stats stats){
    if(prod.size() > 0){
      BaseProduce<?>[] arr = prod.values().toArray(new BaseProduce<?>[0]);
      Arrays.sort(arr, (a, b) -> a.type().id() - b.type().id());
      
      for(BaseProduce<?> p : arr){
        p.display(stats);
      }
    }
  }
}
