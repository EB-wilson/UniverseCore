package universeCore.world.consumers;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Floatf;
import arc.func.Floatp;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.*;

public abstract class BaseConsume<T extends Building & ConsumerBuildComp>{
  public BaseConsumers parent;
  
  /**消耗的类型*/
  public abstract UncConsumeType<?> type();
  
  public abstract void consume(T entity);
  public abstract void update(T entity);
  public abstract void display(Stats stats);
  public abstract void build(T entity, Table table);
  public abstract boolean valid(T entity);

  public abstract Object[] filter(T entity);
}
