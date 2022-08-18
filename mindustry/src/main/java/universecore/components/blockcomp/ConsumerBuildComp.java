package universecore.components.blockcomp;

import arc.struct.ObjectMap;
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
  
  @BindField("consData")
  default ObjectMap<Class<?>, Object> consData(){
    return null;
  }
  
  /**获得该块的ConsumerBlock*/
  default ConsumerBlockComp getConsumerBlock(){
    return getBlock(ConsumerBlockComp.class);
  }
  
  @SuppressWarnings("unchecked")
  default <T> T consData(Class<T> clazz){
    return (T)consData().get(clazz);
  }
  
  @SuppressWarnings("unchecked")
  default <T> T consData(Class<T> clazz, T def){
    return (T)consData().get(clazz, def);
  }
  
  default <T> void consData(T object){
    consData().put(object.getClass(), object);
  }
  
  /**获得该块的NuclearEnergyBlock*/
  default ConsumerBuildComp getConsumerBuilding(){
    return getBlock(ConsumerBuildComp.class);
  }
  
  default boolean productionValid(){
    return getBuilding().productionValid();
  }

  default boolean consValid(){
    return consumer().valid();
  }
  
  default boolean shouldConsume(){
    return consumer() != null && consumer().hasOptional() || consumeCurrent() != -1;
  }
}
