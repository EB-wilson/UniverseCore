package universeCore.entityComps.blockComps;

import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.Time;
import mindustry.world.blocks.power.PowerGraph;
import mindustry.world.consumers.Consumers;
import universeCore.util.handler.FieldHandler;
import universeCore.world.blockModule.BaseConsumeModule;
import universeCore.world.consumers.BaseConsume;
import universeCore.world.consumers.BaseConsumers;

/**消耗者组件，令方块具有进行资源消耗与资源检查的能力
 * @author EBwilson */
public interface ConsumerBuildComp extends BuildCompBase{
  int consumeCurrent();
  
  BaseConsumeModule consumer();
  
  ObjectMap<Class<?>, Object> consData();
  
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
  
  default float consumeMultiplier(BaseConsume<?> cons){
    return 1;
  }
  
  default float consDelta(BaseConsumers cons){
    return cons.acceptOverdrive? getBuilding().edelta(): getBuilding().efficiency()*Time.delta;
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
