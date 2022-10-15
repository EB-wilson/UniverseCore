package universecore.components.blockcomp;

import universecore.annotations.Annotations;
import universecore.annotations.Annotations.BindField;
import universecore.world.blocks.modules.BaseConsumeModule;

/**消耗者组件，令方块具有进行资源消耗与资源检查的能力
 * @author EBwilson
 * @since 1.0*/
public interface ConsumerBuildComp extends BuildCompBase{
  @BindField("consumeCurrent")
  default int consumeCurrent(){
    return 0;
  }
  
  @BindField("consumer")
  default BaseConsumeModule consumer(){
    return null;
  }

  @Annotations.MethodEntry(entryMethod = "update")
  default void updateProducer(){
    consumer().update();
  }

  default float consEfficiency(){
    return consumer().consEfficiency;
  }
  
  /**获得该块的ConsumerBlock*/
  default ConsumerBlockComp getConsumerBlock(){
    return getBlock(ConsumerBlockComp.class);
  }
  
  /**获得该块的NuclearEnergyBlock*/
  default ConsumerBuildComp getConsumerBuilding(){
    return getBlock(ConsumerBuildComp.class);
  }

  default boolean consumeValid(){
    return consumer() == null || !consumer().hasConsume() || consumer().valid();
  }
  
  default boolean shouldConsume(){
    return consumer() != null && consumer().hasOptional() || consumeCurrent() != -1;
  }
}
