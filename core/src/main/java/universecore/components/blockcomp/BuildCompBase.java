package universecore.components.blockcomp;

import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.modules.ItemModule;
import mindustry.world.modules.LiquidModule;
import universecore.annotations.Annotations;

/**建筑组件的基本接口，其实应该使用一个组建管理器构建建筑等，但anuke的做法使得这很困难
 *
 * @author EBwilson
 * @since 1.0*/
public interface BuildCompBase{
  /**泛型检查的获取建筑的方块，这需要该实现类是{@link Building}的子类
   *
   * @param clazz 返回的方块类型*/
  @SuppressWarnings("unchecked")
  default <T> T getBlock(Class<T> clazz){
    Block block = getBuilding().block;
    if(clazz.isAssignableFrom(block.getClass())){
      return (T) block;
    }
    return null;
  }

  /**获取此建筑的{@link Block}，需要*/
  default Block getBlock(){
    return getBlock(Block.class);
  }
  
  /**有检查的泛型获取该类型实例
   * @param clazz 返回的基类类型
   *
   * @throws ClassCastException 如果包含该接口的类并不是{@link Building}的子类*/
  @SuppressWarnings("unchecked")
  default <T> T getBuilding(Class<T> clazz){
    if(clazz.isAssignableFrom(getClass())) return (T)this;
    throw new ClassCastException(getClass() + " cannot cast to " + clazz);
  }

  /**获取Building
   *
   * @throws ClassCastException 如果包含该接口的类并不是{@link Building}的子类*/
  default Building getBuilding(){
    if(Building.class.isAssignableFrom(getClass())) return (Building) this;
    throw new ClassCastException(getClass() + " cannot cast to " + Building.class);
  }

  /**无检查的泛型获取该类型实例*/
  @SuppressWarnings("unchecked")
  default <T> T getBuild(){
    return (T) this;
  }

  /**获取该方块的tile*/
  @Annotations.BindField("tile")
  default Tile getTile(){
    return null;
  }

  /**获得items模块*/
  @Annotations.BindField("items")
  default ItemModule items(){
    return null;
  }

  /**获得liquids模块*/
  @Annotations.BindField("liquids")
  default LiquidModule liquids(){
    return null;
  }
}
