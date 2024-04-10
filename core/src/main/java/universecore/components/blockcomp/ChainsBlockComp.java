package universecore.components.blockcomp;

import mindustry.world.meta.Stats;
import universecore.annotations.Annotations;
import universecore.world.meta.UncStat;

/**链式方块的方块信息组件，用于block，为{@linkplain ChainsBuildComp 链式建筑}提供必要的描述属性
 *
 * @since 1.5
 * @author EBwilson*/
public interface ChainsBlockComp{
  /**一个连续结构的最大x轴跨度*/
  @Annotations.BindField("maxChainsWidth")
  default int maxWidth(){
    return 0;
  }

  /**一个连续结构的最大y轴跨度*/
  @Annotations.BindField("maxChainsHeight")
  default int maxHeight(){
    return 0;
  }

  /**这个方块是否能与目标方块组成连续结构，需要两个块之间互相都能够链接才能构成连续结构
   *
   * @param other 目标方块*/
  default boolean chainable(ChainsBlockComp other){
    return getClass().isAssignableFrom(other.getClass());
  }

  /**设置方块的统计数据，通常你不需要操作这个行为*/
  @Annotations.MethodEntry(entryMethod = "setStats", context = "stats -> stats")
  default void setChainsStats(Stats stats){
    stats.add(UncStat.maxStructureSize, "@x@", maxWidth(), maxHeight());
  }
}
