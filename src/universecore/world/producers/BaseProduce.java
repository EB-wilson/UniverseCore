package universecore.world.producers;

import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ProducerBuildComp;

public abstract class BaseProduce<T extends Building & ProducerBuildComp>{
  public static final TextureRegion EMPTY_TEX = new TextureRegion();
  
  public BaseProducers parent;
  
  /**产出资源类型*/
  public abstract ProduceType<?> type();
  
  public TextureRegion icon(){
    return EMPTY_TEX;
  }
  
  public abstract void produce(T entity);
  public abstract void update(T entity);
  public abstract void display(Stats stats);
  public boolean valid(T entity){
    return true;
  }
  
  public void dump(T entity){}
}
