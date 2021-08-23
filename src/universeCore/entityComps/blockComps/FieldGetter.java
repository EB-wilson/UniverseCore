package universeCore.entityComps.blockComps;

import java.lang.reflect.Field;

public interface FieldGetter{
  @SuppressWarnings("unchecked")
  default <T> T getField(Class<T> type, String name){
    try{
      Class<?> clazz = this.getClass();
      Field field = clazz.getField(name);
      return (T) field.get(this);
    }catch(NoSuchFieldException | IllegalAccessException e){
      throw new RuntimeException("field error getting! caused by:" + e);
    }
  }
}
