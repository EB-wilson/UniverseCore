package universeCore.entityComps.blockComps;

import java.lang.reflect.Field;

/**为实现的类提供获取一个获得公共字段的方法
 * @author EBwilson */
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
