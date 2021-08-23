package universeCore.entityComps.blockComps;

import universeCore.world.blockModule.BaseConsumeModule;
import universeCore.world.consumers.UncConsumeType;

/**����������������н�����Դ��������ñ�ǵ�����
 * ���봴���ı�����
 * <pre>{@code
 *   AneConsumeModule [consumer]
 * }<pre/>
 * ��ʹ�÷�Ĭ����������Ҫ��д���÷���*/
public interface ConsumerBuildComp extends BuildCompBase, FieldGetter{
  int consumeCurrent();
  
  /**��øÿ��NuclearEnergyBlock*/
  default ConsumerBlockComp getConsumerBlock(){
    return getBlock(ConsumerBlockComp.class);
  }
  
  /**��øÿ��NuclearEnergyBlock*/
  default ConsumerBuildComp getConsumerBuilding(){
    return getBlock(ConsumerBuildComp.class);
  }
  
  default <T> T getBuilding(UncConsumeType<?, T> type){
    return getBuilding(type.getRequire());
  }
  
  default BaseConsumeModule consumer(){
    return getField(BaseConsumeModule.class, "consumer");
  }
  
  default boolean productionValid(){
    return getBuilding().productionValid();
  }
  
  default boolean shouldConsume(){
    return consumer() != null && consumer().hasOptional() || consumeCurrent() != -1;
  }
  
  default void updateConsume(){
    /*�����䷽Ҫ�����δѡ���䷽ʱ���������ĸ���*/
    if(consumer() != null && (consumer().hasOptional() || consumer().hasConsume()) && consumeCurrent() != -1){
      consumer().update();
    }
  }
}
