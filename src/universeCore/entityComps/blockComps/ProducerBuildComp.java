package universeCore.entityComps.blockComps;


import universeCore.world.blockModule.BaseProductModule;
import universeCore.world.producers.ProduceType;

/**生产者组件，令方块具有按需进行资源生产输出的能力
 * 必须创建的变量：
 * <pre>{@code
 *   AneProductModule [producer]
 * }<pre/>
 * 若使用非默认命名则需要重写调用方法*/
public interface ProducerBuildComp extends BuildCompBase, FieldGetter{
  int produceCurrent();
  
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
  
  default boolean consValid(){
    return getBuilding().consValid();
  }
  
  default boolean shouldConsume(){
    return getBuilding().shouldConsume();
  }
  
  default boolean productionValid(){
    return getBuilding().productionValid();
  }
  
  default <T> T getBuilding(ProduceType<?, T> type){
    return getBuilding(type.getRequire());
  }
}
