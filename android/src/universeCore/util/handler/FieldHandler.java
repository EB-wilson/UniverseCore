package universeCore.util.handler;

import arc.util.Log;
import sun.misc.Unsafe;

import java.lang.reflect.*;

/**通过反射更改属性值的工具类*/
public class FieldHandler{
  /**改变指定对象的选中属性的值，
  * 将无视该属性的访问修饰符和final修饰符
  * @param key 要进行更改的属性名称
  * @param object 要更改属性值的对象
  * @param value 要写入的值
  * @return 被更改的值
  * */
  public static Object setValue(Class<?> clazz, String key, Object object, Object value){
    try{
      return setValue(clazz.getDeclaredField(key), object, value);
    }catch(NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }
  
  /**改变指定对象的选中属性的值，
  * 将无视该属性的访问修饰符和final修饰符
  * @param field 需要执行更改的域
  * @param object 要更改属性值的对象
  * @param value 要写入的值
  * @return 被更改的值
  * */
  public static Object setValue(Field field, Object object, Object value){
    Log.debug("doing" + object);
    Object least = getValue(field, object);
    
    field.setAccessible(true);
    try{
      field.set(object, value);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  
    return least;
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T getValue(Object target, String key){
    try{
      Field field = target.getClass().getDeclaredField(key);
      return (T) getValue(field, target);
    }catch(NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }
  
  public static <T> T getValue(Class<?> clazz, String key, Object object){
    try{
      return getValue(clazz.getDeclaredField(key), object);
    }catch(NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }
  
  @SuppressWarnings("unchecked")
  private static <T> T getValue(Field field, Object object){
    field.setAccessible(true);
    try{
      return (T) field.get(object);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T[] getArray(Field field, Object object, Class<T> componentType){
    if(field.getType().isArray()){
      Object[] arr = getValue(field, object);
      
      T[] newArr = (T[]) Array.newInstance(componentType, arr.length);
      for(int i=0; i<arr.length; i++){
        newArr[i] = (T)arr[i];
      }
      
      return newArr;
    }
    return null;
  }
}
