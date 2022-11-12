package universecore.components.blockcomp;

import universecore.annotations.Annotations;

/**拼接方块的建筑组件，记录一些{@linkplain  SpliceBuildComp 拼接方块建筑}必须的属性
 *
 * @since 1.5
 * @author EBwilson*/
public interface SpliceBlockComp extends ChainsBlockComp{
  @Annotations.BindField("interCorner")
  default boolean interCorner(){
    return false;
  }

  @Annotations.BindField("negativeSplice")
  default boolean negativeSplice(){
    return false;
  }
}
