package universeCore.util.aspect;

import arc.Events;
import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.game.EventType;
import universeCore.util.handler.EventsHandler;
import universeCore.util.handler.FieldHandler;

import java.lang.reflect.Field;

public class AspectManager{
  private final Seq<AbstractAspect<?>> aspects = new Seq<>();
  
  private final ObjectMap<EventType.Trigger, Seq<AbstractAspect<?>>> triggers = new ObjectMap<>();
  private final ObjectMap<EventType.Trigger, Runnable> triggerRunnable = new ObjectMap<>();
  
  public AspectManager(){
    Runnable r;
    for(EventType.Trigger trigger : EventType.Trigger.values()){
      r = () -> {
        Seq<AbstractAspect<?>> trs = triggers.get(trigger);
        if(trs == null) return;
        for(AbstractAspect<?> a: trs){
          a.handle();
        }
      };
      triggerRunnable.put(trigger, r);
    }
    assignTrigger();
  }
  
  public void update(){}
  
  public void add(AbstractAspect<?> aspect){
    EventType.Trigger trigger = aspect.setTrigger();
    if(trigger != null){
      triggers.get(trigger, Seq::new).add(aspect);
    }
    aspects.add(aspect);
  }
  
  public void assignTrigger(){
    for(ObjectMap.Entry<EventType.Trigger, Runnable> entry : triggerRunnable){
      Events.run(entry.key, entry.value);
    }
  }
  
  public void designTrigger(){
    for(ObjectMap.Entry<EventType.Trigger, Runnable> entry : triggerRunnable){
      EventsHandler.remove(entry.key, entry.value);
    }
  }
}
