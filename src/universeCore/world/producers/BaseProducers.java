package universeCore.world.producers;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.util.Structs;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.world.meta.Stats;
import universeCore.util.UncLiquidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class BaseProducers{
  protected final HashMap<ProduceType<?, ?>, BaseProduce> prod = new HashMap<>();

  /**仅在动态配方生效，用于显示选择配方的图标*/
  public TextureRegion icon;
  /**仅在动态配方生效，用于显示选择配方的顶部颜色*/
  public Color color;

  public BaseProducers setIcon(TextureRegion icon){
    this.icon = icon;
    return this;
  }

  public BaseProducers setColor(Color color){
    this.color = color;
    return this;
  }
  
  public ProduceItems item(Item item, int amount){
    return items(new ItemStack[]{new ItemStack(item, amount)});
  }
  
  public ProduceItems items(ItemStack[] items){
    return add(new ProduceItems(items));
  }
  
  public ProduceLiquids liquid(Liquid liquid, float amount){
    return liquids(new UncLiquidStack[]{new UncLiquidStack(liquid, amount)});
  }
  
  public ProduceLiquids liquids(UncLiquidStack[] liquids){
    return add(new ProduceLiquids(liquids));
  }
  
  public ProducePower power(float prod){
    return add(new ProducePower(prod));
  }
  
  public <T extends BaseProduce> T add(T produce){
    prod.put(produce.type(), produce);
    return produce;
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseProduce> T get(ProduceType<T, ?> type){
    return (T) prod.get(type);
  }

  public BaseProduce[] all(){
    return prod.values().toArray(new BaseProduce[0]);
  }

  public void remove(ProduceType<?, ?> type){
    prod.remove(type);
  }

  public void display(Stats stats){
    if(prod.size() > 0) for(BaseProduce p: prod.values()){
      p.display(stats);
    }
  }
}
