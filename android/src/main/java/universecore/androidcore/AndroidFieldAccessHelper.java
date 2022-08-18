package universecore.androidcore;

import arc.struct.ObjectMap;
import universecore.util.FieldAccessHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@SuppressWarnings("DuplicatedCode")
public class AndroidFieldAccessHelper implements FieldAccessHelper{
  private static final ObjectMap<Class<?>, ObjectMap<String, Field>> fieldPool = new ObjectMap<>();

  protected static Field getField(Class<?> clazz, String field){
    return fieldPool.get(clazz, ObjectMap::new).get(field, () -> {
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

        f.setAccessible(true);
        return f;
      }catch(NoSuchFieldException e){
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void set(Object object, String field, Object value){
    try{
      getField(object.getClass(), field).set(object, value);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setStatic(Class<?> clazz, String field, Object value){
    try{
      getField(clazz, field).set(null, value);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Object object, String field){
    try{
      return (T) getField(object.getClass(), field).get(object);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getStatic(Class<?> clazz, String field){
    try{
      return (T) getField(clazz, field).get(null);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }
}
