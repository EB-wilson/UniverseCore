package universeCore.world.producers;

import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ProducerBuildComp;

public abstract class BaseProduce{
  /**产出资源类型*/
  public abstract ProduceType<?, ?> type();
  
  public abstract void produce(ProducerBuildComp entity);
  public abstract void update(ProducerBuildComp entity);
  public abstract void display(Stats stats);
  public boolean valid(ProducerBuildComp entity){
    return true;
  }
  
  public void dump(ProducerBuildComp entity){}
}
