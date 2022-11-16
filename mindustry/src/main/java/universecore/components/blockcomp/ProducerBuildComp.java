package universecore.components.blockcomp;


import universecore.annotations.Annotations;
import universecore.world.blocks.modules.BaseProductModule;

/**生产者组件，令方块具有按需进行资源生产输出的能力
 *
 * @author EBwilson
 * @since 1.0*/
public interface ProducerBuildComp extends BuildCompBase, ConsumerBuildComp{
  /**当前选择的生产项目的索引*/
  default int produceCurrent(){
    return consumeCurrent();
  }

  /**生产组件*/
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

  /**当前生产是否可用*/
  default boolean productValid(){
    return producer() == null || producer().valid();
  }

  /**当前是否应当执行生产项更新*/
  default boolean shouldProduct(){
    return producer() != null && produceCurrent() != -1;
  }
}
