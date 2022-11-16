package universecore.components.blockcomp;

import arc.math.geom.Point2;
import universecore.annotations.Annotations;
import universecore.world.DirEdges;

import java.util.Arrays;

/**拼接方块的建筑组件，使方块会自动同步周围的方块状态并记录，通常用于绘制方块连续的连接材质
 *<p><strong>这是个不稳定的API，后续可能会调整为更加通用且高效的形式，这会造成API变更，慎用</strong>
 *
 * @since 1.5
 * @author EBwilson*/
public interface SpliceBuildComp extends ChainsBuildComp{
  @Annotations.BindField("splice")
  default int[] splice(){
    return null;
  }

  @Annotations.BindField("splice")
  default void splice(int[] arr){}

  default boolean[] getSplice(){
    boolean[] result = new boolean[8];

    t: for(int i=0; i<8; i++){
      SpliceBuildComp other = null;
      for(Point2 p: DirEdges.get8(getBlock().size, i)){
        if(other == null){
          if(getBuilding().nearby(p.x, p.y) instanceof SpliceBuildComp oth && oth.chains().container == chains().container){
            other = oth;
          }
          else{
            result[i] = false;
            continue t;
          }
        }
        else if(other != getBuilding().nearby(p.x, p.y)){
          result[i] = false;
          continue t;
        }
      }
      result[i] = true;
    }

    return result;
  }

  default int[] getRegionBits(){
    int[] result = new int[8];
    Arrays.fill(result, -1);

    boolean[] data = getSplice();

    boolean neg = getBlock(SpliceBlockComp.class).negativeSplice();
    for(int part = 0; part < 8; part++){
      if(part < 4){
        if(neg){
          result[part] = !data[part*2]? 0: -1;
        }
        else result[part] = data[part*2]? 0: -1;
      }
      else{
        int i = (part - 4)*2, b = (i+2)%8;
        result[part] = !data[i] && !data[b]? (neg? 0: -1)
            : data[i] && (getBlock(SpliceBlockComp.class).interCorner() || !data[i + 1]) && data[b]? 1: -1;
      }
    }

    return result;
  }

  @Annotations.MethodEntry(entryMethod = "onProximityUpdate")
  default void updateRegionBit(){
    splice(getRegionBits());
  }
}
