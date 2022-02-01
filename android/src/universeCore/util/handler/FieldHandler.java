package universeCore.util.handler;

import arc.struct.Seq;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**字段操作的静态方法集合，包含了读取，写入等操作，所有引用抛出异常都被catch并封装到{@link RuntimeException}，无需手动try或抛出
 * <p>安卓的实现基于反射，性能较低，不建议过于频繁的调用
 * @author EBwilson
 * @since 1.0*/
public class FieldHandler{
  /**不考虑访问修饰符，对目标类获得字段，如果在类中没有找到，则向上递归引用，直到调用到{@link Object}的一级继承类
   * @param clazz 获取字段的目标类型
   * @param name 字段名称
   * @return 获取到的字段
   * @throws NoSuchFieldException 如果引用到{@link Object}类的直接继承类型时依然没有找到此字段*/
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
  
  /**设定指定对象的选中属性值，将无视该属性的访问修饰符和final修饰符
  * @param key 要进行更改的属性名称
  * @param object 要更改属性值的对象
  * @param value 要写入的值
  * @return 字段原有的值*/
  public static Object setValue(Class<?> clazz, String key, Object object, Object value){
    try{
      return setValue(getDeclaredFieldSuper(clazz, key), object, value);
    }catch(NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }
  
  /**设定指定对象的选中属性的值，将无视该属性的访问修饰符和final修饰符
  * @param field 需要执行更改的字段
  * @param object 要更改属性值的对象
  * @param value 要写入的值
  * @return 字段原有的值*/
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
  
  /**从目标对象中获取指定的字段值，并返回它，如果字段不存在则会以抛出异常结束
   * @param target 目标对象
   * @param key 字段名称
   * @return 字段的值，如果它存在的话*/
  public static <T> T getValue(Object target, String key){
    try{
      Field field = getDeclaredFieldSuper(target.getClass(), key);
      return getValue(field, target);
    }catch(NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }
  
  /**从指明的类获取指定的字段，获得它在指定对象中的值，并返回它，如果字段不存在则会以抛出异常结束
   * <p>如果字段是静态的，则传入的目标对象可以为null，否则会以异常结束
   * <p>这个方法不会向上递归搜索字段，字段一定来源于指明的类
   * @param clazz 要获取字段的类
   * @param key 字段名称
   * @param target 目标对象
   * @return 字段的值，如果它存在的话
   * @throws NullPointerException 如果字段不是静态的，同时传入null作为目标对象*/
  public static <T> T getValue(Class<?> clazz, String key, Object target){
    try{
      return getValue(clazz.getDeclaredField(key), target);
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
  
  /**从指定的字段获取一个数组，这个数组由指定的元素类型生成以避免类型转换错误
   * <p><strong>注意，这不代表你可以任意指定类型，如果类型不兼容仍然会抛出异常
   * <p>此方法返回的是数组的拷贝而并非数组本身，修改此数组不会对来源产生影响</strong>
   * @param field 获取数组的字段
   * @param object 获取字段的对象，如果字段是静态的，则可以为null，否则将抛出异常
   * @param componentType 数组的元素类型
   * @return 具有指定元素类型的数组，包含了来源数组的信息
   * @throws ClassCastException 如果指定的元素类型与来源数组的元素类型不兼容*/
  @SuppressWarnings("unchecked")
  public static <T> T[] getArray(Field field, Object object, Class<T> componentType){
    if(field.getType().isArray()){
      if(!field.getType().getComponentType().isAssignableFrom(componentType)) throw new ClassCastException(componentType.getName() + " is not assignable for " + field.getType().getComponentType());
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
