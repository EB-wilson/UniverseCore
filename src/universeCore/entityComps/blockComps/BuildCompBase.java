package universeCore.entityComps.blockComps;

import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.modules.ItemModule;
import mindustry.world.modules.LiquidModule;

public interface BuildCompBase extends FieldGetter{
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
    return null;
  }
  
  default Building getBuilding(){
    if(this instanceof Building) return (Building)this;
    return null;
  }
  
  default ItemModule items(){
    return getField(ItemModule.class, "items");
  }
  
  default LiquidModule liquids(){
    return getField(LiquidModule.class, "liquids");
  }
}
