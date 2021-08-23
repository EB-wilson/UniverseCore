package universeCore.entityComps.blockComps;


import universeCore.world.blockModule.BaseProductModule;
import universeCore.world.producers.ProduceType;

/**����������������а��������Դ�������������
 * ���봴���ı�����
 * <pre>{@code
 *   AneProductModule [producer]
 * }<pre/>
 * ��ʹ�÷�Ĭ����������Ҫ��д���÷���*/
public interface ProducerBuildComp extends BuildCompBase, FieldGetter{
  int produceCurrent();
  
  default BaseProductModule producer(){
    return getField(BaseProductModule.class, "producer");
  }
  
  /**��øÿ��NuclearEnergyBlock*/
  default ProducerBuildComp getProducerBlock(){
    return getBlock(ProducerBuildComp.class);
  }
  
  /**��øÿ��NuclearEnergyBlock*/
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
