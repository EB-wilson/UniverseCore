package universecore.util.aspect;

import java.util.HashMap;
import java.util.LinkedHashSet;

public abstract class BaseTriggerControl<Trigger extends BaseTriggerEntry<?>>{
  protected final HashMap<Trigger, LinkedHashSet<AbstractAspect<?, ?>>> aspectEntry = new HashMap<>();
  
  public abstract void apply(Trigger triggerEntry);
  
  public abstract void remove(Trigger triggerEntry);
}
