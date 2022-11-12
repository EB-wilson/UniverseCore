package universecore.components.blockcomp;

import mindustry.world.meta.Stats;
import universecore.annotations.Annotations;
import universecore.world.meta.UncStat;

/**链式方块的方块信息组件，用于block，为{@linkplain ChainsBuildComp 链式建筑}提供必要的描述属性
 *
 * @since 1.5
 * @author EBwilson*/
public interface ChainsBlockComp{
  @Annotations.BindField("maxChainsWidth")
  default int maxWidth(){
    return 0;
  }

  @Annotations.BindField("maxChainsHeight")
  default int maxHeight(){
    return 0;
  }

  default boolean chainable(ChainsBlockComp other){
    return getClass().isAssignableFrom(other.getClass());
  }

  @Annotations.MethodEntry(entryMethod = "setStats", context = "stats -> stats")
  default void setChainsStats(Stats stats){
    stats.add(UncStat.maxStructureSize, "@x@", maxWidth(), maxHeight());
  }
}
