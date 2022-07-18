package universecore.util.aspect.triggers;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.OrderedSet;
import mindustry.game.EventType;
import universecore.util.aspect.AbstractAspect;
import universecore.util.aspect.BaseTriggerControl;

import java.util.LinkedHashSet;

@SuppressWarnings({"unchecked", "rawtypes"})
public class TriggerControl extends BaseTriggerControl<TriggerEntry<?>>{
  private final ObjectMap<EventType.Trigger, Runnable> triggerRunnable = new ObjectMap<>();
  
  private final ObjectMap<EventType.Trigger, OrderedSet<TriggerEntry<?>>> triggers = new ObjectMap<>();
  
  {
    Runnable r;
    for(EventType.Trigger trigger : EventType.Trigger.values()){
      r = () -> {
        OrderedSet<TriggerEntry<?>> entries = triggers.get(trigger);
        if(entries != null){
          for(TriggerEntry entry : entries){
            for(AbstractAspect<?, ?> aspect : aspectEntry.get(entry)){
              aspect.run(entry);
            }
          }
        }
      };
      triggerRunnable.put(trigger, r);
      Events.run(trigger, r);
    }
  }
  
  @Override
  public void apply(TriggerEntry<?> triggerEntry){
    triggers.get(triggerEntry.trigger, OrderedSet::new).add(triggerEntry);
    aspectEntry.computeIfAbsent(triggerEntry, e -> new LinkedHashSet<>()).add(triggerEntry.aspect);
  }
  
  @Override
  public void remove(TriggerEntry<?> triggerEntry){
    OrderedSet<TriggerEntry<?>> entries = triggers.get(triggerEntry.trigger);
    if(entries != null) entries.remove(triggerEntry);
    LinkedHashSet<AbstractAspect<?, ?>> aspects = aspectEntry.get(triggerEntry);
    if(aspects != null) aspects.remove(triggerEntry.aspect);
  }
}
