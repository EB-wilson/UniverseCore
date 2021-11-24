package universeCore.entityComps.blockComps;

import mindustry.content.Items;
import mindustry.world.meta.Stats;
import universeCore.world.producers.BaseProduce;
import universeCore.world.producers.BaseProducers;

import java.util.ArrayList;

/**生产者方块的组件，令方块具有记录输出资源配方的功能
 * @author EBwilson */
public interface ProducerBlockComp extends ConsumerBlockComp{
  ArrayList<BaseProducers> producers();
  
  default BaseProducers newProduce(){
    BaseProducers produce = new BaseProducers();
    producers().add(produce);
    return produce;
  }
  
  default void setProducerStats(Stats stats){
    if(producers().size() > 1){
      for(int i=0; i<consumers().size(); i++){
        for(BaseProduce<?> p: producers().get(i).all()){
          p.display(recipeTable().stats[i]);
        }
        recipeTable().build();
      }
    }
    else if(producers().size() == 1){
      for(BaseProduce<?> prod: producers().get(0).all()){
        prod.display(stats);
      }
    }
  }
  
  /**初始化匹配消耗生产列表，在init()最后调用*/
  default void initProduct(){
    int b = producers().size();
    int a = consumers().size();
    /*控制produce添加/移除配方以使配方同步*/
    while(a > b){
      BaseProducers p = new BaseProducers();
      p.item(Items.copper, 1);
      producers().add(p);
      b++;
    }
    while(a < b){
      b--;
      producers().remove(b);
    }
    
    for(int index = 0; index< producers().size(); index++){
      producers().get(index).cons = consumers().get(index);
    }
  }
}
