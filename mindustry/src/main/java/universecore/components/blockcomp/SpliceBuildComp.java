package universecore.components.blockcomp;

import arc.math.geom.Point2;
import universecore.annotations.Annotations;
import universecore.world.DirEdges;

/**拼接方块的建筑组件，使方块会自动同步周围的方块状态并记录，通常用于绘制方块连续的连接材质
 * <p><strong>这是个不稳定的API，后续可能会调整为更加通用且高效的形式，这会造成API变更，慎用</strong>
 *
 * @since 1.5
 * @author EBwilson*/
public interface SpliceBuildComp extends ChainsBuildComp{
  @Annotations.BindField("splice")
  default int splice(){
    return 0;
  }

  @Annotations.BindField("splice")
  default void splice(int arr){}

  default int getSplice(){
    int result = 0;

    t: for(int i=0; i<8; i++){
      SpliceBuildComp other = null;
      for(Point2 p: DirEdges.get8(getBlock().size, i)){
        if(other == null){
          if(getBuilding().nearby(p.x, p.y) instanceof SpliceBuildComp oth && oth.chains().container == chains().container){
            other = oth;
          }
          else{
            continue t;
          }
        }
        else if(other != getBuilding().nearby(p.x, p.y)){
          continue t;
        }
      }
      result |= 1 << i;
    }

    return result;
  }

  @Annotations.MethodEntry(entryMethod = "onProximityUpdate")
  default void updateRegionBit(){
    splice(getSplice());
  }
}
