package universeCore.util.aspect.triggers;

import universeCore.util.aspect.AbstractAspect;

public abstract class BaseTriggerEntry<T>{
  public final Class<?> controlType;
  
  public AbstractAspect<T, ?> aspect;
  
  public BaseTriggerEntry(Class<?> type){
    controlType = type;
  }
  
  public abstract void handle(Object... child);
}
