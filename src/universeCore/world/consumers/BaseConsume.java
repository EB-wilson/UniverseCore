package universeCore.world.consumers;

import arc.scene.ui.layout.Table;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ConsumerBuildComp;

public abstract class BaseConsume{
  /**消耗的类型*/
  public abstract UncConsumeType<?, ?> type();
  
  public abstract void consume(ConsumerBuildComp entity);
  public abstract void update(ConsumerBuildComp entity);
  public abstract void display(Stats stats);
  public abstract void build(ConsumerBuildComp entity, Table table);
  public abstract boolean valid(ConsumerBuildComp entity);

  public abstract Object[] filter(ConsumerBuildComp entity);
}
