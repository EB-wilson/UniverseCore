package universecore.components.blockcomp;

import universecore.annotations.Annotations;

/**工厂方块组件，描述{@linkplain FactoryBuildComp 工厂建筑}中必要的一些属性
 *
 * @since 1.4
 * @author EBwilson*/
public interface FactoryBlockComp extends ProducerBlockComp{
  /**方块的热机效率，由0-1的插值，为方块从启动到最大效率的速度，这是{@link arc.math.Mathf#lerpDelta(float, float, float)}的插值*/
  @Annotations.BindField("warmupSpeed")
  default float warmupSpeed(){
    return 0;
  }

  /**方块的冷却速度，由0-1的插值，为方块完全停机的速度，这是{@link arc.math.Mathf#lerpDelta(float, float, float)}的插值*/
  @Annotations.BindField("stopSpeed")
  default float stopSpeed(){
    return 0;
  }
}
