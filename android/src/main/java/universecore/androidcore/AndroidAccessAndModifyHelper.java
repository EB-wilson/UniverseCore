package universecore.androidcore;

import universecore.util.AccessAndModifyHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AndroidAccessAndModifyHelper implements AccessAndModifyHelper{
  @Override
  public void set(Object object, Field field, Object value){
    try{
      field.set(object, value);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setStatic(Class<?> clazz, Field field, Object value){
    try{
      field.set(null, value);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setAccessible(Field field){
    field.setAccessible(true);
  }

  @Override
  public void setAccessible(Method method){
    method.setAccessible(true);
  }

  @Override
  public <T> void setAccessible(Constructor<T> cstr){
    cstr.setAccessible(true);
  }
}
