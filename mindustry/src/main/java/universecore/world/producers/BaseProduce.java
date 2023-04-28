package universecore.world.producers;

import arc.func.Floatf;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ProducerBuildComp;
import universecore.world.consumers.BaseConsume;

public abstract class BaseProduce<T extends ProducerBuildComp>{
  public Floatf<T> prodMultiplier;
  
  public BaseProducers parent;
  public boolean blockWhenFull;

  /**产出资源类型*/
  public abstract ProduceType<?> type();

  public Color color(){
    return null;
  }

  public boolean shouldBlockWhenFull(){
    return blockWhenFull;
  }

  public boolean hasIcons(){
    return true;
  }

  public abstract void buildIcons(Table table);
  public abstract void merge(BaseProduce<T> other);
  public abstract void produce(T entity);
  public abstract void update(T entity);
  public abstract void display(Stats stats);
  public abstract boolean valid(T entity);
  
  public void dump(T entity){}

  public float multiple(T entity){
    return prodMultiplier == null? 1: prodMultiplier.get(entity);
  }

  @SuppressWarnings("unchecked")
  public <N extends ProducerBuildComp> BaseProduce<T> setMultiple(Floatf<N> multiple){
    prodMultiplier = (Floatf<T>) multiple;
    return this;
  }
}
