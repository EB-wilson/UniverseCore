package universecore.util.aspect.triggers;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.OrderedSet;
import universecore.util.aspect.AbstractAspect;
import universecore.util.aspect.BaseTriggerControl;

import java.util.LinkedHashSet;

public class EventControl extends BaseTriggerControl<EventEntry<?, ?>>{
  private final ObjectMap<Class<?>, OrderedSet<EventEntry<?, ?>>> events = new ObjectMap<>();
  
  @SuppressWarnings("unchecked")
  private void setListener(Class<?> event, EventEntry<?, ?> entry){
    OrderedSet<EventEntry<?, ?>> entries = events.get(event);
    if(entries == null){
      entries = new OrderedSet<>();
      events.put(event, entries);
      OrderedSet<EventEntry<?, ?>> finalEntries = entries;
      Events.on(event, e -> {
        for(EventEntry eventEntry : finalEntries){
          for(AbstractAspect<?, ?> aspect : aspectEntry.get(eventEntry)){
            aspect.run(eventEntry);
          }
        }
      });
    }
    entries.add(entry);
  }
  
  @Override
  public void apply(EventEntry<?, ?> triggerEntry){
    aspectEntry.computeIfAbsent(triggerEntry, e -> new LinkedHashSet<>()).add(triggerEntry.aspect);
    setListener(triggerEntry.eventType, triggerEntry);
  }
  
  @Override
  public void remove(EventEntry<?, ?> triggerEntry){
    OrderedSet<EventEntry<?, ?>> entries = events.get(triggerEntry.eventType);
    if(entries != null) entries.remove(triggerEntry);
    LinkedHashSet<AbstractAspect<?, ?>> aspects = aspectEntry.get(triggerEntry);
    if(aspects != null) aspects.remove(triggerEntry.aspect);
  }
}
