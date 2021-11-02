package universeCore.entityComps.blockComps;


import arc.util.Time;
import mindustry.world.consumers.Consumers;
import universeCore.world.blockModule.BaseProductModule;
import universeCore.world.consumers.BaseConsumers;
import universeCore.world.producers.BaseProduce;
import universeCore.world.producers.BaseProducers;

/**生产者组件，令方块具有按需进行资源生产输出的能力
 * 必须创建的变量：
 * <pre>{@code
 *   AneProductModule [producer]
 * }<pre/>
 * 若使用非默认命名则需要重写调用方法
 * @author EBwilson*/
public interface ProducerBuildComp extends BuildCompBase, FieldGetter, ConsumerBuildComp{
  default int produceCurrent(){
    return consumeCurrent();
  }
  
  default BaseProductModule producer(){
    return getField(BaseProductModule.class, "producer");
  }
  
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
