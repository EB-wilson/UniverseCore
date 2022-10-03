package universecore.components.blockcomp;

import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.modules.ItemModule;
import mindustry.world.modules.LiquidModule;
import universecore.annotations.Annotations;

/**建筑组件的基本接口，其实应该使用一个组建管理器构建建筑等，但anuke的做法使得这很困难
 * @author EBwilson
 * @since 1.0*/
public interface BuildCompBase{
  @SuppressWarnings("unchecked")
  default <T> T getBlock(Class<T> clazz){
    Block block = getBuilding().block;
    if(clazz.isAssignableFrom(block.getClass())){
      return (T) block;
    }
    return null;
  }
  
  default Block getBlock(){
    return getBlock(Block.class);
  }
  
  /**用于获取该方块的Building*/
  @SuppressWarnings("unchecked")
  default <T> T getBuilding(Class<T> clazz){
    if(clazz.isAssignableFrom(getClass())) return (T)this;
    throw new ClassCastException(getClass() + " cannot cast to " + clazz);
  }
  
  default Building getBuilding(){
    if(Building.class.isAssignableFrom(getClass())) return (Building) this;
    throw new ClassCastException(getClass() + " cannot cast to " + Building.class);
  }

  @SuppressWarnings("unchecked")
  default <T> T getBuild(){
    return (T) this;
  }

  default Tile getTile(){
    return getBuilding().tile;
  }
  
  @Annotations.BindField("items")
  default ItemModule items(){
    return null;
  }
  
  @Annotations.BindField("liquids")
  default LiquidModule liquids(){
    return null;
  }
}
