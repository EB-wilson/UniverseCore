package universecore.util.handler;

import arc.Events;
import arc.func.Boolf;
import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;

import java.lang.reflect.Field;

public class EventsHandler{
  private static final ObjectMap<Object, Seq<Cons<?>>> listeners = FieldHandler.getValueDefault(Events.class, "events");
  
  public static void getCons(Object event, Runnable listener, Cons<Cons<?>> task){
    for(Cons<?> cons : listeners.get(event)){
      Field[] f = cons.getClass().getSuperclass().getDeclaredFields();
      if(f.length != 1) continue;
      if(f[0].getType() == Runnable.class){
        try{
          f[0].setAccessible(true);
          if(f[0].get(cons) == listener) task.get(cons);
        }
        catch(IllegalAccessException e){
          e.printStackTrace();
        }
      }
    }
  }
  
  public static void getCons(Object event, Runnable listener, Boolf<Object> criterion, Cons<Cons<?>> task, Class<?>... fieldTypes){
    for(Cons<?> cons : listeners.get(event)){
      Field[] f = cons.getClass().getSuperclass().getDeclaredFields();
      if(f.length != 1) continue;
      if(f[0].getType() == Runnable.class){
        try{
          f[0].setAccessible(true);
          if(f[0].get(cons) == listener && isTarget(listener, criterion, fieldTypes)) task.get(cons);
        }
        catch(IllegalAccessException e){
          e.printStackTrace();
        }
      }
    }
  }
  
  public static void getCons(Class<?> event, Boolf<Object> criterion, Cons<Cons<?>> task, Class<?>... fieldTypes){
    Events.fireWrap(event, new Object(), cons -> {
      if(isTarget(cons, criterion, fieldTypes)) task.get(cons);
    });
  }
  
  public static boolean isTarget(Object lambda, Boolf<Object> criterion, Class<?>... fieldTypes){
    Field[] f = lambda.getClass().getSuperclass().getDeclaredFields();
    if(f.length != fieldTypes.length) return false;
    for(int i=0; i< fieldTypes.length; i++){
      Class<?> type = fieldTypes[0];
      if(f[i].getType() == type){
        try{
          f[0].setAccessible(true);
          if(!criterion.get(f[0].get(lambda))) return false;
        }catch(IllegalAccessException e){
          Log.info(e);
          return false;
        }
      }
    }
    return true;
  }
  
  public static void remove(Class<?> event, Boolf<Object> criterion, Class<?>... fieldTypes){
    getCons(event, criterion, e -> listeners.get(event, Seq::new).remove(e), fieldTypes);
  }
  
  public static void remove(Object event, Runnable listener){
    getCons(event, listener, e -> listeners.get(event, Seq::new).remove(e));
  }
  
  public static void remove(Object event, Runnable listener, Boolf<Object> criterion, Class<?>... fieldTypes){
    getCons(event, listener, criterion, e -> listeners.get(event, Seq::new).remove(e), fieldTypes);
  }
}
