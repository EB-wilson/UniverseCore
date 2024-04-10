package universecore.world.consumers;

import arc.func.Floatf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.ctype.Content;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;

public abstract class BaseConsume<T extends ConsumerBuildComp>{
  public BaseConsumers parent;
  public Floatf<T> consMultiplier;


  /**消耗的类型*/
  public abstract ConsumeType<?> type();

  public boolean hasIcons(){
    return true;
  }

  public abstract void buildIcons(Table table);

  public abstract void merge(BaseConsume<T> other);
  
  public abstract void consume(T entity);
  public abstract void update(T entity);
  public abstract void display(Stats stats);
  public abstract void build(T entity, Table table);
  public void buildBars(T entity, Table bars){}
  public abstract float efficiency(T entity);

  public abstract Seq<Content> filter();

  public float multiple(T entity){
    return (consMultiplier == null? 1: consMultiplier.get(entity))*entity.consMultiplier();
  }

  @SuppressWarnings("unchecked")
  public <N extends ConsumerBuildComp> BaseConsume<T> setMultiple(Floatf<N> multiple){
    consMultiplier = (Floatf<T>) multiple;
    return this;
  }
}
