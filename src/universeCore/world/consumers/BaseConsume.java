package universeCore.world.consumers;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import mindustry.gen.Building;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.*;

public abstract class BaseConsume<T extends Building & ConsumerBuildComp>{
  public static final TextureRegion EMPTY_TEX = new TextureRegion();
  public BaseConsumers parent;
  
  /**消耗的类型*/
  public abstract UncConsumeType<?> type();
  
  public TextureRegion icon(){
    return EMPTY_TEX;
  }
  
  public abstract void consume(T entity);
  public abstract void update(T entity);
  public abstract void display(Stats stats);
  public abstract void build(T entity, Table table);
  public abstract boolean valid(T entity);

  public abstract Bits filter(T entity);
}
