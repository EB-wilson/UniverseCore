package universeCore.entityComps.blockComps;

import arc.func.Cons2;
import arc.math.Mathf;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.meta.Stats;
import universeCore.world.consumers.BaseConsumers;
import universeCore.world.consumers.UncConsumeType;

import java.util.ArrayList;

/**Consume组件，为方块添加可标记消耗的功能
 * 必须创建的变量：
 * <pre>{@code
 *   ArrayList<AneConsumers> [consumers]
 *   ArrayList<AneConsumers> [optionalCons]
 * }<pre/>
 * 若使用非默认命名则需要重写调用方法*/
public interface ConsumerBlockComp extends BuildCompBase, FieldGetter{
  @SuppressWarnings("unchecked")
  default ArrayList<BaseConsumers> consumers(){
    return getField(ArrayList.class, "consumers");
  }
  
  @SuppressWarnings("unchecked")
  default ArrayList<BaseConsumers> optionalCons(){
    return getField(ArrayList.class, "optionalCons");
  }
  
  default boolean oneOfOptionCons(){
    return getField(boolean.class, "oneOfOptionCons");
  }
  
  @SuppressWarnings("UnusedReturnValue")
  default BaseConsumers newConsume(){
    BaseConsumers consume = new BaseConsumers(false);
    consumers().add(consume);
    return consume;
  }
  
  default BaseConsumers newOptionalConsume(Cons2<Building, BaseConsumers> validDef, Cons2<Stats, BaseConsumers> displayDef){
    BaseConsumers consume = new BaseConsumers(true){
      {
        method = validDef;
        display = displayDef;
      }
    };
    optionalCons().add(consume);
    return consume;
  }
  
  default void initPower(float powerCapacity){
    Block block = (Block)this;
    block.consumes.add(new ConsumePower(0 ,powerCapacity, powerCapacity > 0){
      @Override
      public float requestedPower(Building e){
        ConsumerBuildComp entity = (ConsumerBuildComp)e;
        if(entity.consumer().current == null) return 0f;
        if(entity.getBuilding().tile().build == null || entity.consumeCurrent() == -1 || !entity.consumer().excludeValid(2)) return 0f;
        if(buffered){
          return (1f-entity.getBuilding().power.status)*capacity;
        }
        else{
          return entity.consumer().current.get(UncConsumeType.power).usage * Mathf.num(entity.shouldConsume());
        }
      }
    });
  }
}
