package universecore.components.blockcomp;

import universecore.annotations.Annotations;

/**工厂方块组件，描述{@linkplain FactoryBuildComp 工厂建筑}中必要的一些属性*/
public interface FactoryBlockComp extends ProducerBlockComp{
  @Annotations.BindField("warmupSpeed")
  default float warmupSpeed(){
    return 0;
  }

  @Annotations.BindField("stopSpeed")
  default float stopSpeed(){
    return 0;
  }
}
