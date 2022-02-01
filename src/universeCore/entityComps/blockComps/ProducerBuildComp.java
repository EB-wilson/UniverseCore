package universeCore.entityComps.blockComps;


import universeCore.world.blockModule.BaseProductModule;
import universeCore.world.producers.BaseProduce;
import universeCore.world.producers.BaseProducers;

/**生产者组件，令方块具有按需进行资源生产输出的能力
 * @author EBwilson
 * @since 1.0*/
public interface ProducerBuildComp extends BuildCompBase, ConsumerBuildComp{
  default int produceCurrent(){
    return consumeCurrent();
  }
  
  BaseProductModule producer();
  
  /**获得该块的NuclearEnergyBlock*/
  default ProducerBuildComp getProducerBlock(){
    return getBlock(ProducerBuildComp.class);
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
