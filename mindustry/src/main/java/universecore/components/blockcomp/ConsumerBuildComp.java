package universecore.components.blockcomp;

import arc.scene.ui.layout.Table;
import universecore.annotations.Annotations;
import universecore.annotations.Annotations.BindField;
import universecore.world.blocks.modules.BaseConsumeModule;
import universecore.world.consumers.BaseConsume;
import universecore.world.consumers.BaseConsumers;

/**消耗者组件，令方块具有进行资源消耗与资源检查的能力
 * @author EBwilson
 * @since 1.0*/
public interface ConsumerBuildComp extends BuildCompBase{
  /**当前已选中的消耗项索引*/
  @BindField("consumeCurrent")
  default int consumeCurrent(){
    return 0;
  }

  /**获取消耗模块*/
  @BindField("consumer")
  default BaseConsumeModule consumer(){
    return null;
  }

  @Annotations.MethodEntry(entryMethod = "update")
  default void updateConsumer(){
    consumer().update();
  }

  /**当前的消耗执行效率，从0-1*/
  default float consEfficiency(){
    return consumer().consEfficiency;
  }

  default float optionalConsEff(BaseConsumers consumers){
    return consumer().getOptionalEff(consumers);
  }
  
  /**获得该块的ConsumerBlock*/
  default ConsumerBlockComp getConsumerBlock(){
    return getBlock(ConsumerBlockComp.class);
  }
  
  /**获得该块的NuclearEnergyBlock*/
  default ConsumerBuildComp getConsumerBuilding(){
    return getBlock(ConsumerBuildComp.class);
  }

  /**这个方块当前的消耗列表的消耗条件是否满足*/
  default boolean consumeValid(){
    return consumer() == null || !consumer().hasConsume() || consumer().valid();
  }

  /**这个方块当前是否应该对消耗列表执行消耗*/
  default boolean shouldConsume(){
    return consumer() != null && consumeCurrent() != -1;
  }

  /**这个方块当前是否应该对可选消耗列表执行消耗*/
  default boolean shouldConsumeOptions(){
    return shouldConsume() && consumer().hasOptional();
  }

  @SuppressWarnings("unchecked")
  default void buildConsumerBars(Table bars){
    if (consumer().current != null){
      for (BaseConsume<? extends ConsumerBuildComp> consume : consumer().current.all()) {
        ((BaseConsume<ConsumerBuildComp>) consume).buildBars(this, bars);
      }
    }
  }
}
