package universecore.components.blockcomp;

import arc.struct.Seq;
import mindustry.world.meta.Stats;
import universecore.annotations.Annotations;
import universecore.world.producers.BaseProduce;
import universecore.world.producers.BaseProducers;

/**生产者方块的组件，令方块具有记录输出资源配方的功能
 *
 * @author EBwilson
 * @since 1.0*/
public interface ProducerBlockComp extends ConsumerBlockComp{
  /**生产清单的列表*/
  @Annotations.BindField(value = "producers", initialize = "new arc.struct.Seq<>()")
  default Seq<BaseProducers> producers(){
    return null;
  }

  /**创建一张新的生产清单加入容器，并返回它*/
  default BaseProducers newProduce(){
    BaseProducers produce = new BaseProducers();
    producers().add(produce);
    return produce;
  }
  
  /**初始化匹配消耗生产列表，在init()最后调用*/
  @Annotations.MethodEntry(entryMethod = "init")
  default void initProduct(){
    int b = producers().size;
    int a = consumers().size;
    /*控制produce添加/移除配方以使配方同步*/
    while(a > b){
      BaseProducers p = new BaseProducers();
      producers().add(p);
      b++;
    }
    while(a < b){
      b--;
      producers().remove(b);
    }
    
    for(int index = 0; index< producers().size; index++){
      producers().get(index).cons = consumers().get(index);
    }
  }
}
