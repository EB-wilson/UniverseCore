package universecore.world.consumers;

import arc.func.Floatf;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;

public abstract class BaseConsume<T extends ConsumerBuildComp>{
  public static TextureRegion EMP = new TextureRegion();

  public BaseConsumers parent;
  public Floatf<T> consMultiplier;

  /**消耗的类型*/
  public abstract UncConsumeType<?> type();
  
  public TextureRegion icon(){
    return EMP;
  }

  public abstract void merge(BaseConsume<T> other);
  
  public abstract void consume(T entity);
  public abstract void update(T entity);
  public abstract void display(Stats stats);
  public abstract void build(T entity, Table table);
  public abstract float efficiency(T entity);

  public abstract Bits filter(T entity);

  public float multiple(T entity){
    return consMultiplier == null? 1: consMultiplier.get(entity);
  }

  @SuppressWarnings("unchecked")
  public <N extends ConsumerBuildComp> BaseConsume<T> setMultiple(Floatf<N> multiple){
    consMultiplier = (Floatf<T>) multiple;
    return this;
  }
}
