package universecore.util.aspect.triggers;

import arc.struct.ObjectMap;
import arc.struct.OrderedSet;
import universecore.util.aspect.AbstractAspect;

public abstract class BaseTriggerControl<Trigger extends BaseTriggerEntry<?>>{
  protected final ObjectMap<Trigger, OrderedSet<AbstractAspect<?, ?>>> aspectEntry = new ObjectMap<>();
  
  public abstract void apply(Trigger triggerEntry);
  
  public abstract void remove(Trigger triggerEntry);
}
