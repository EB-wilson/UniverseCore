package universeCore.util.aspect.triggers;

import arc.func.Cons;
import mindustry.game.EventType;

public class TriggerEntry<T> extends BaseTriggerEntry<T>{
  public final EventType.Trigger trigger;
  public final Cons<T> handle;
  
  public TriggerEntry(EventType.Trigger trigger, Cons<T> handle){
    super(TriggerControl.class);
    this.handle = handle;
    this.trigger = trigger;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void handle(Object... child){
    handle.get((T) child[0]);
  }
}
