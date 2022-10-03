package universecore.androidcore;

import arc.struct.ObjectMap;
import universecore.util.FieldAccessHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("DuplicatedCode")
public class AndroidFieldAccessHelper implements FieldAccessHelper{
  private static final ObjectMap<String, Field> EMP = new ObjectMap<>();
  private static final ObjectMap<Class<?>, ObjectMap<String, Field>> fieldMap = new ObjectMap<>();

  private static final Method getFieldMethod;

  static {
    try {
      getFieldMethod = Class.class.getMethod("getDeclaredField", String.class);
      getFieldMethod.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

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

    throw new NoSuchFieldException("field " + field + " was not found in class: " + clazz);
  }

  protected Field getField0(Class<?> clazz, String field) throws NoSuchFieldException{
    Field res;
    try {
      res = (Field) getFieldMethod.invoke(clazz, field);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new NoSuchFieldException();
    }
    res.setAccessible(true);

    return res;
  }

  @Override
  public void set(Object object, String field, Object value){
    try{
      getField(object.getClass(), field, false).set(object, value);
    }catch(IllegalAccessException | NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setStatic(Class<?> clazz, String field, Object value){
    try{
      getField(clazz, field, true).set(null, value);
    }catch(IllegalAccessException | NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Object object, String field){
    try{
      return (T) getField(object.getClass(), field, false).get(object);
    }catch(IllegalAccessException | NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getStatic(Class<?> clazz, String field){
    try{
      return (T) getField(clazz, field, true).get(null);
    }catch(IllegalAccessException | NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }
}
