package universeCore.entityComps.blockComps;

import universeCore.world.blockModule.BaseConsumeModule;
import universeCore.world.consumers.UncConsumeType;

/**消耗者组件，令方块具有进行资源消耗与可用标记的能力
 * 必须创建的变量：
 * <pre>{@code
 *   AneConsumeModule [consumer]
 * }<pre/>
 * 若使用非默认命名则需要重写调用方法*/
public interface ConsumerBuildComp extends BuildCompBase, FieldGetter{
  int consumeCurrent();
  
  /**获得该块的NuclearEnergyBlock*/
  default ConsumerBlockComp getConsumerBlock(){
    return getBlock(ConsumerBlockComp.class);
  }
  
  /**获得该块的NuclearEnergyBlock*/
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
    /*当无配方要求或者未选择配方时不进行消耗更新*/
    if(consumer() != null && (consumer().hasOptional() || consumer().hasConsume()) && consumeCurrent() != -1){
      consumer().update();
    }
  }
}
