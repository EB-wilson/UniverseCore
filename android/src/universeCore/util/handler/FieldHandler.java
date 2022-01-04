package universeCore.util.handler;

import arc.struct.Seq;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**通过反射更改属性值的工具类*/
public class FieldHandler{
  public static Field getDeclaredFieldSuper(Class<?> clazz, String name) throws NoSuchFieldException{
    Class<?> current = clazz;
    Seq<Field> checking = new Seq<>();
    
    while(current != Object.class){
      checking.addAll(current.getDeclaredFields());
      
      Field result = checking.find(f -> f.getName().equals(name));
      if(result != null) return result;
      
      current = current.getSuperclass();
    }
    
    throw new NoSuchFieldException("no such field \"" + name + "\" found in " + clazz + " and super class!");
  }
  
  /**改变指定对象的选中属性的值，
  * 将无视该属性的访问修饰符和final修饰符
  * @param key 要进行更改的属性名称
  * @param object 要更改属性值的对象
  * @param value 要写入的值
  * @return 被更改的值
  * */
  public static Object setValue(Class<?> clazz, String key, Object object, Object value){
    try{
      return setValue(getDeclaredFieldSuper(clazz, key), object, value);
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
      Field field = getDeclaredFieldSuper(target.getClass(), key);
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
    throw new RuntimeException("field isn't an array type");
  }
}
