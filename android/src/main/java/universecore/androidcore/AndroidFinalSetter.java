package universecore.androidcore;

import universecore.util.FinalSetter;

import java.lang.reflect.Field;

public class AndroidFinalSetter implements FinalSetter{
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
}
