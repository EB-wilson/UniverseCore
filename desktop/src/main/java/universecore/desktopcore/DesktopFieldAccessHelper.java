package universecore.desktopcore;

import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import universecore.util.FieldAccessHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings("unchecked")
public class DesktopFieldAccessHelper implements FieldAccessHelper{
  private static final boolean useUnsafe;
  private static final Field modifiers;

  static {
    boolean tmp;
    try{
      Class.forName("sun.misc.Unsafe");
      tmp = true;
    }catch(ClassNotFoundException e){
      tmp = false;
    }
    useUnsafe = tmp;

    try {
      modifiers = Field.class.getDeclaredField("modifiers");
      modifiers.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  private static final ObjectMap<Class<?>, ObjectMap<String, Field>> fieldMap = new ObjectMap<>();
  private static final ObjectMap<String, Field> EMP = new ObjectMap<>();

  private static final ObjectSet<Field> finalFields = new ObjectSet<>();

  private static final ObjectMap<Field, MethodHandle> getters = new ObjectMap<>();
  private static final ObjectMap<Field, MethodHandle> setters = new ObjectMap<>();

  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public Field getField(Class<?> clazz, String field, boolean isStatic) throws NoSuchFieldException{
    Field res = fieldMap.get(clazz, EMP).get(field);
    if(res != null) return res;

    try{
      if(isStatic){
        return getField0(clazz, field);
      }
      else{
        Class<?> curr = clazz;
        while(curr != null){
          try{
            return getField0(curr, field);
          }
          catch(NoSuchFieldException ignored){}

          curr = curr.getSuperclass();
        }
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }

    throw new NoSuchFieldException();
  }

  protected Field getField0(Class<?> clazz, String field) throws NoSuchFieldException{
    Field res = clazz.getDeclaredField(field);
    res.setAccessible(true);

    if((res.getModifiers() & Modifier.FINAL) != 0){
      try{
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.set(res, res.getModifiers() & ~Modifier.FINAL);

        finalFields.add(res);
      }catch(Throwable e){
        throw new RuntimeException(e);
      }
    }

    return res;
  }

  protected void initField(Field field){
    getters.get(field, () -> {
      try{
        return lookup.unreflectGetter(field);
      }catch(IllegalAccessException e){
        throw new RuntimeException(e);
      }
    });
    setters.get(field, () -> {
      try{
        return lookup.unreflectSetter(field);
      }catch(IllegalAccessException e){
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void set(Object object, String field, Object value){
    try{
      Field f = getField(object.getClass(), field, false);
      if(useUnsafe){
        UnsafeAccess.set(object, f, value);
      }
      else{
        if(finalFields.contains(f)){
          f.set(object, value);
          return;
        }

        initField(f);
        setters.get(f).invoke(object, value);
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setStatic(Class<?> clazz, String field, Object value){
    try{
      Field f = getField(clazz, field, false);
      if(useUnsafe){
        UnsafeAccess.setStatic(f, value);
      }
      else{
        if(finalFields.contains(f)){
          f.set(null, value);
          return;
        }

        initField(f);
        setters.get(f).invoke(value);
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T get(Object object, String field){
    try{
      Field f = getField(object.getClass(), field, false);
      if(useUnsafe){
        return (T) UnsafeAccess.get(object, f);
      }
      else{
        initField(f);
        return (T) getters.get(f).invoke(object);
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T getStatic(Class<?> clazz, String field){
    try{
      Field f = getField(clazz, field, true);
      if(useUnsafe){
        return (T) UnsafeAccess.getStatic(f);
      }
      else{
        initField(f);
        return (T) getters.get(f).invoke();
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }
}
