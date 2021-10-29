package universeCore.world.producers;

import mindustry.gen.Building;
import mindustry.world.Build;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ProducerBuildComp;

public abstract class BaseProduce<T extends Building & ProducerBuildComp>{
  public BaseProducers parent;
  
  /**产出资源类型*/
  public abstract ProduceType<?> type();
  
  public abstract void produce(T entity);
  public abstract void update(T entity);
  public abstract void display(Stats stats);
  public boolean valid(T entity){
    return true;
  }
  
  public void dump(T entity){}
}
