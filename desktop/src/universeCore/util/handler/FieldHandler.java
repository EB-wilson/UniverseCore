package universeCore.util.handler;

import arc.util.Log;
import sun.misc.Unsafe;

import java.lang.reflect.*;

/**通过反射更改属性值的工具类*/
public class FieldHandler{
  private final static Unsafe unsafe;
  
  static{
    Unsafe temp;
    try{
      Constructor<Unsafe> cstr = Unsafe.class.getDeclaredConstructor();
      cstr.setAccessible(true);
      temp = cstr.newInstance();
    }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
      throw new RuntimeException(e);
    }
    unsafe = temp;
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
    Object least = getValue(field, object);
    boolean isStatic = Modifier.isStatic(field.getModifiers());
  
    if(value instanceof Integer){
      setInt(field, object, (Integer)value, isStatic);
    }
    else if(value instanceof Float){
      setFloat(field, object, (Float)value, isStatic);
    }
    else if(value instanceof Boolean){
      setBoolean(field, object, (Boolean)value, isStatic);
    }
    else if(value instanceof Short){
      setShort(field, object, (Short)value, isStatic);
    }
    else if(value instanceof Byte){
      setByte(field, object, (Byte)value, isStatic);
    }
    else if(value instanceof Long){
      setLong(field, object, (Long)value, isStatic);
    }
    else if(value instanceof Double){
      setDouble(field, object, (Double)value, isStatic);
    }
    else{
      setObject(field, object, value, isStatic);
    }
    
    return least;
  }
  
  private static void setInt(Field field, Object object, int value, boolean isStatic){
    unsafe.putInt(isStatic? unsafe.staticFieldBase(field): object,
      isStatic? unsafe.staticFieldOffset(field): unsafe.objectFieldOffset(field),
      value);
  }
  
  private static void setFloat(Field field, Object object, float value, boolean isStatic){
    unsafe.putFloat(isStatic? unsafe.staticFieldBase(field): object,
      isStatic? unsafe.staticFieldOffset(field): unsafe.objectFieldOffset(field),
      value);
  }
  
  private static void setBoolean(Field field, Object object, boolean value, boolean isStatic){
    unsafe.putBoolean(isStatic? unsafe.staticFieldBase(field): object,
      isStatic? unsafe.staticFieldOffset(field): unsafe.objectFieldOffset(field),
      value);
  }
  
  private static void setLong(Field field, Object object, long value, boolean isStatic){
    unsafe.putLong(isStatic? unsafe.staticFieldBase(field): object,
      isStatic? unsafe.staticFieldOffset(field): unsafe.objectFieldOffset(field),
      value);
  }
  
  private static void setDouble(Field field, Object object, double value, boolean isStatic){
    unsafe.putDouble(isStatic? unsafe.staticFieldBase(field): object,
      isStatic? unsafe.staticFieldOffset(field): unsafe.objectFieldOffset(field),
      value);
  }
  
  private static void setShort(Field field, Object object, short value, boolean isStatic){
    unsafe.putShort(isStatic? unsafe.staticFieldBase(field): object,
      isStatic? unsafe.staticFieldOffset(field): unsafe.objectFieldOffset(field),
      value);
  }
  
  private static void setByte(Field field, Object object, byte value, boolean isStatic){
    unsafe.putByte(isStatic? unsafe.staticFieldBase(field): object,
      isStatic? unsafe.staticFieldOffset(field): unsafe.objectFieldOffset(field),
      value);
  }
  
  private static void setObject(Field field, Object object, Object value, boolean isStatic){
    unsafe.putObject(isStatic? unsafe.staticFieldBase(field): object,
      isStatic? unsafe.staticFieldOffset(field): unsafe.objectFieldOffset(field),
      value);
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
    boolean isStatic = Modifier.isStatic(field.getModifiers());
     return (T)unsafe.getObject(isStatic? unsafe.staticFieldBase(field): object,
      isStatic? unsafe.staticFieldOffset(field): unsafe.objectFieldOffset(field));
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
