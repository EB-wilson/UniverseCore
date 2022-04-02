package universecore.components.blockcomp;


import universecore.annotations.Annotations;
import universecore.world.blocks.modules.BaseProductModule;
import universecore.world.producers.BaseProduce;
import universecore.world.producers.BaseProducers;

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
  
  /**获得该块的NuclearEnergyBlock*/
  default ProducerBlockComp getProducerBlock(){
    return getBlock(ProducerBlockComp.class);
  }
  
  /**获得该块的NuclearEnergyBlock*/
  default ProducerBuildComp getProducerBuilding(){
    return getBlock(ProducerBuildComp.class);
  }

  default float consDelta(BaseProducers prod){
    return consDelta(prod.cons);
  }
  
  default float productMultiplier(BaseProduce<?> prod){
    return 1;
  }

  default boolean consValid(){
    return getBuilding().consValid();
  }
  
  default boolean shouldConsume(){
    return getBuilding().shouldConsume();
  }
  
  default boolean productionValid(){
    return getBuilding().productionValid();
  }
}
