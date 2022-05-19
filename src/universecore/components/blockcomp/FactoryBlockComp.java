package universecore.components.blockcomp;

import universecore.annotations.Annotations;

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
