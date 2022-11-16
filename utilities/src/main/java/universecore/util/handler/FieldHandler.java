package universecore.util.handler;

import universecore.ImpCore;

import java.util.WeakHashMap;

/**字段操作的静态方法集合，包含了读取，写入等操作，所有引用抛出异常都被catch并封装到{@link RuntimeException}，无需手动try或抛出
 * @author EBwilson
 * @since 1.0*/
@SuppressWarnings({"unchecked", "rawtypes"})
public class FieldHandler<T>{
  private static final WeakHashMap<Class, FieldHandler> defaultHandlers = new WeakHashMap<>();

  private final Class<T> clazz;

  public FieldHandler(Class<T> clazz){
    this.clazz = clazz;
  }
  
  /**设定指定对象的选中属性值，将无视该属性的访问修饰符和final修饰符，如果目标对象为null，则设置的字段为static。
   * <n>除非字段是静态的，否则不允许传入空目标对象
   *
   * @param object 要更改属性值的对象
   * @param key 要进行更改的属性名称
   * @param value 要写入的值
   * @throws NullPointerException 若传入的目标对象为null同时字段不是静态的*/
  public void setValue(T object, String key, Object value){
    if(object == null) ImpCore.fieldAccessHelper.setStatic(clazz, key, value);
    else ImpCore.fieldAccessHelper.set(object, key, value);
  }

  /**使用默认规则构造一个处理器对象并缓存，使用这个默认处理器来执行setValue操作
   * @see FieldHandler#setValue(Object, String, Object) */
  public static void setValueDefault(Object obj, String key, Object value){
    defaultHandlers.computeIfAbsent(obj.getClass(), e -> new FieldHandler(obj.getClass())).setValue(obj, key, value);
  }

  /**使用默认规则构造一个处理器对象并缓存，使用这个默认处理器来执行静态的getValue操作
   * @see FieldHandler#setValue(Object, String, Object) */
  public static void setValueDefault(Class<?> clazz, String key, Object value){
    defaultHandlers.computeIfAbsent(clazz, e -> new FieldHandler(clazz)).setValue(null, key, value);
  }

  /**使用默认规则构造一个处理器对象，使用这个默认处理器来执行setValue操作，但这并不会缓存这个处理器
   * @see FieldHandler#setValue(Object, String, Object) */
  public static void setValueTemp(Object obj, String key, Object value){
    new FieldHandler(obj.getClass()).setValue(obj, key, value);
  }

  /**使用默认规则构造一个处理器对象，使用这个默认处理器来执行静态的setValue操作，但这并不会缓存这个处理器
   * @see FieldHandler#setValue(Object, String, Object) */
  public static void setValueTemp(Class<?> clazz, String key, Object value){
    new FieldHandler(clazz).setValue(null, key, value);
  }
  
  /**获取指定的字段值，并返回它，如果字段不存在则会以抛出异常结束，如果目标对象为null，则设置的字段为static
   * 除非字段是静态的，否则不允许传入空目标对象
   *
   * @param object 目标对象，若字段非静态则要求不为空，若字段为静态的，将忽略此参数
   * @param key 字段名称
   * @return 字段的值，如果它存在的话
   * @throws NullPointerException 若传入的目标对象为null同时字段不是静态的*/
  public <R> R getValue(T object, String key){
    return object == null? ImpCore.fieldAccessHelper.getStatic(clazz, key): ImpCore.fieldAccessHelper.get(object, key);
  }

  /**使用默认规则构造一个处理器对象并缓存，使用这个默认处理器来执行getValue操作
   * @see FieldHandler#getValue(Object, String)*/
  public static <T> T getValueDefault(Object obj, String key){
    return (T) defaultHandlers.computeIfAbsent(obj.getClass(), e -> new FieldHandler(obj.getClass())).getValue(obj, key);
  }

  /**使用默认规则构造一个处理器对象并缓存，使用这个默认处理器来执行静态的getValue操作
   * @see FieldHandler#getValue(Object, String)*/
  public static <T> T getValueDefault(Class<?> clazz, String key){
    return (T) defaultHandlers.computeIfAbsent(clazz, e -> new FieldHandler(clazz)).getValue(null, key);
  }

  /**使用默认规则构造一个处理器对象，使用这个默认处理器来执行getValue操作，但这并不会缓存这个处理器
   * @see FieldHandler#getValue(Object, String)*/
  public static <T> T getValueTemp(Object obj, String key){
    return (T) new FieldHandler(obj.getClass()).getValue(obj, key);
  }

  /**使用默认规则构造一个处理器对象，使用这个默认处理器来执行静态的getValue操作，但这并不会缓存这个处理器
   * @see FieldHandler#getValue(Object, String)*/
  public static <T> T getValueTemp(Class<?> clazz, String key){
    return (T) new FieldHandler(clazz).getValue(null, key);
  }

  /**清空所有当前缓存的处理器*/
  public static void clearDefault(){
    defaultHandlers.clear();
  }
}
