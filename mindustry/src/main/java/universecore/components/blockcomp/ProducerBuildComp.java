package universecore.components.blockcomp;


import universecore.annotations.Annotations;
import universecore.world.blocks.modules.BaseProductModule;

/**生产者组件，令方块具有按需进行资源生产输出的能力
 * @author EBwilson
 * @since 1.0*/
public interface ProducerBuildComp extends BuildCompBase, ConsumerBuildComp{
  default int produceCurrent(){
    return consumeCurrent();
  }
 
  @Annotations.BindField("producer")
  default BaseProductModule producer(){
    return null;
  }

  @Annotations.MethodEntry(entryMethod = "update")
  default void updateProducer(){
    producer().update();
  }
  
  /**获得该块的NuclearEnergyBlock*/
  default ProducerBlockComp getProducerBlock(){
    return getBlock(ProducerBlockComp.class);
  }
  
  /**获得该块的NuclearEnergyBlock*/
  default ProducerBuildComp getProducerBuilding(){
    return getBlock(ProducerBuildComp.class);
  }

  default boolean productValid(){
    return producer() == null || producer().valid();
  }
  
  default boolean shouldProduct(){
    return producer() != null && produceCurrent() != -1;
  }

}
