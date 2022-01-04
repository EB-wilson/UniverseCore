package universeCore.util.aspect;

import arc.struct.Seq;
import mindustry.game.EventType;

public abstract class AbstractAspect<Type>{
  protected Seq<Type> children = new Seq<>();
  
  public abstract EventType.Trigger setTrigger();
  
  public abstract Seq<Type> source();
  
  public abstract void handle();
  
  public abstract boolean filter();
}
