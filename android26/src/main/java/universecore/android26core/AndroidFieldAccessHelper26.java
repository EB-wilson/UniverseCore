package universecore.android26core;

import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import universecore.util.FieldAccessHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class AndroidFieldAccessHelper26 implements FieldAccessHelper{
  protected static final ObjectMap<Class<?>, ObjectSet<String>> initiled = new ObjectMap<>();
  protected static final ObjectMap<Class<?>, ObjectMap<String, Field>> finalFields = new ObjectMap<>();
  protected static final ObjectMap<Class<?>, ObjectMap<String, MethodHandle>> setters = new ObjectMap<>();
  protected static final ObjectMap<Class<?>, ObjectMap<String, MethodHandle>> getters = new ObjectMap<>();

  protected static final MethodHandles.Lookup lookup = MethodHandles.lookup();
  private static final ObjectMap<String, MethodHandle> EMP_MAP = new ObjectMap<>();
  private static final ObjectMap<String, Field> EMP_FIELD = new ObjectMap<>();

  protected void initField(Class<?> clazz, String field) throws FinalFieldBranch{
    if(initiled.get(clazz, ObjectSet::new).contains(field)) return;

    try{
      Field f = null;
      Class<?> current = clazz;
      ArrayList<Field> checking = new ArrayList<>();

      while(current != Object.class){
        checking.addAll(Arrays.asList(current.getDeclaredFields()));

        Optional<Field> opt = checking.stream().filter(fi -> fi.getName().equals(field)).findFirst();
        if(opt.isPresent()){
          f = opt.get();
          break;
        }

        current = current.getSuperclass();
      }

      if(f == null)
        throw new NoSuchFieldException("no such field \"" + field + "\" found in " + clazz + " and super class!");

      if(Modifier.isFinal(f.getModifiers())){
        finalFields.get(clazz, ObjectMap::new).put(field, f);
      }

      f.setAccessible(true);

      setters.get(clazz, ObjectMap::new).put(field, lookup.unreflectSetter(f));
      getters.get(clazz, ObjectMap::new).put(field, lookup.unreflectGetter(f));

      initiled.get(clazz).add(field);
    }catch(NoSuchFieldException|IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  protected MethodHandle getGetter(Class<?> clazz, String field){
    try{
      initField(clazz, field);
    }catch(FinalFieldBranch ignored){}

    return getters.get(clazz, EMP_MAP).get(field);
  }

  protected MethodHandle getSetter(Class<?> clazz, String field) throws FinalFieldBranch{
    initField(clazz, field);
    Field fin;
    if((fin = finalFields.get(clazz, EMP_FIELD).get(field)) != null)
      throw new FinalFieldBranch(fin);

    return setters.get(clazz, EMP_MAP).get(field);
  }

  @Override
  public void set(Object object, String field, Object value){
    try{
      getSetter(object.getClass(), field).invoke(object, value);
    }catch(FinalFieldBranch branch){
      setFinal(object, branch.source, value);
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setStatic(Class<?> clazz, String field, Object value){
    try{
      getSetter(clazz, field).invoke(value);
    }catch(FinalFieldBranch branch){
      setFinal(null, branch.source, value);
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Object object, String field){
    try{
      return (T) getGetter(object.getClass(), field).invoke(object);
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getStatic(Class<?> clazz, String field){
    try{
      return (T) getGetter(clazz, field).invoke();
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  protected void setFinal(Object object, Field field, Object value){
    try{
      field.set(object, value);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  protected static class FinalFieldBranch extends Throwable{
    public final Field source;

    public FinalFieldBranch(Field f){
      source = f;
    }
  }
}
