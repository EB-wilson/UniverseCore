package universeCore.util.aspect.triggers;

import arc.func.Cons2;

public class EventEntry<T, E> extends BaseTriggerEntry<T>{
  public final Class<E> eventType;
  public final Cons2<E, T> listener;
  
  public EventEntry(Class<E> eventType, Cons2<E, T> handle){
    super(EventControl.class);
    this.eventType = eventType;
    this.listener = handle;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void handle(Object... child){
    listener.get((E)child[0], (T)child[1]);
  }
}
