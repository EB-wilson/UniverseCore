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
import java.util.Objects;

public class BaseProducers{
  protected final ArrayList<BaseProduce> prod = new ArrayList<>(ProduceType.all().length);

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
    if(produce.type().id() >= prod.size()){
      int temp = produce.type().id() - prod.size() + 1;
      for(int i=0; i<temp; i++){
        prod.add(null);
      }
    }
    prod.set(produce.type().id(), produce);
    return produce;
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseProduce> T get(ProduceType<T, ?> type){
    return type.id() >= 0 && type.id() < prod.size()? (T)prod.get(type.id()): null;
  }

  public BaseProduce[] all(){
    return Structs.filter(BaseProduce.class, prod.toArray(new BaseProduce[0]), Objects::nonNull);
  }

  public void remove(int id){
    prod.set(id, null);
  }

  public void display(Stats stats){
    if(prod.size() > 0) for(BaseProduce p: prod){
      p.display(stats);
    }
  }
}
