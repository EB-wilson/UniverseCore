package universecore.util.handler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static universecore.ImpCore.accessAndModifyHelper;

/**字段操作的静态方法集合，包含了读取，写入等操作，所有引用抛出异常都被catch并封装到{@link RuntimeException}，无需手动try或抛出
 * @author EBwilson
 * @since 1.0*/
@SuppressWarnings({"unchecked", "rawtypes"})
public class FieldHandler<T>{
  private static final WeakHashMap<Class, FieldHandler> defaultHandlers = new WeakHashMap<>();

  private final HashMap<String, Field> finalFields = new HashMap<>();
  private final HashMap<String, MethodHandle> getters = new HashMap<>();
  private final HashMap<String, MethodHandle> setters = new HashMap<>();

  private final Class<T> clazz;

  private final MethodHandles.Lookup lookup = MethodHandles.lookup();

  /**不考虑访问修饰符，对目标类获得字段，如果在类中没有找到，则向上递归引用，直到调用到{@link Object}的一级继承类
   * @param clazz 获取字段的目标类型
   * @param name 字段名称
   * @return 获取到的字段
   * @throws NoSuchFieldException 如果引用到{@link Object}类的直接继承类型时依然没有找到此字段*/
  public static Field getDeclaredFieldSuper(Class<?> clazz, String name) throws NoSuchFieldException{
    Class<?> current = clazz;
    ArrayList<Field> checking = new ArrayList<>();
    
    while(current != Object.class){
      checking.addAll(Arrays.asList(current.getDeclaredFields()));
      
      Optional<Field> opt = checking.stream().filter(f -> f.getName().equals(name)).findFirst();
      if(opt.isPresent()){
        return opt.get();
      }

      current = current.getSuperclass();
    }
    
    throw new NoSuchFieldException("no such field \"" + name + "\" found in " + clazz + " and super class!");
  }

  public FieldHandler(Class<T> clazz){
    this.clazz = clazz;
  }
  
  /**设定指定对象的选中属性值，将无视该属性的访问修饰符和final修饰符
   * @param object 要更改属性值的对象
   * @param key 要进行更改的属性名称
   * @param value 要写入的值  */
  public void setValue(T object, String key, Object value){
    if(finalFields.containsKey(key)){
      if(object == null){
        if(!Modifier.isStatic(finalFields.get(key).getModifiers())) throw new NullPointerException("field " + key + " is not static, but target object is null");
        setFinalValueStatic(key, value);
      }
      else setFinalValue(object, key, value);
      return;
    }

    MethodHandle setter = setters.computeIfAbsent(key, e -> {
      try{
        Field field = getDeclaredFieldSuper(clazz, key);
        accessAndModifyHelper.setAccessible(field);
        if(Modifier.isFinal(field.getModifiers())){
          finalFields.put(key, field);
          setValue(object, key, value);
          return null;
        }
        return lookup.unreflectSetter(field);
      }catch(IllegalAccessException|NoSuchFieldException ex){
        throw new RuntimeException(ex);
      }
    });

    if(setter == null) return;

    try{
      if(setter.type().parameterArray().length == 1){
        setter.invoke(value);
      }
      else{
        if(object == null)
          throw new NullPointerException("field " + key + " is not a static field, bug given object was null");
        setter.invoke(object, value);
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  public static void setValueDefault(Object obj, String key, Object value){
    defaultHandlers.computeIfAbsent(obj.getClass(), e -> new FieldHandler(obj.getClass())).setValue(obj, key, value);
  }

  public static void setValueDefault(Class<?> clazz, String key, Object value){
    defaultHandlers.computeIfAbsent(clazz, e -> new FieldHandler(clazz)).setValue(null, key, value);
  }

  public static void setValueTemp(Object obj, String key, Object value){
    new FieldHandler(obj.getClass()).setValue(obj, key, value);
  }

  public static void setValueTemp(Class<?> clazz, String key, Object value){
    new FieldHandler(clazz).setValue(null, key, value);
  }
  
  /**获取指定的字段值，并返回它，如果字段不存在则会以抛出异常结束
   * 除非字段是静态的，否则不允许传入空目标对象
   * @param object 目标对象，若字段非静态则要求不为空，若字段为静态的，将忽略此参数
   * @param key 字段名称
   * @return 字段的值，如果它存在的话
   * @throws NullPointerException 若传入的目标对象为null同时字段不是静态的*/
  public <R> R getValue(T object, String key){
    MethodHandle getter = getters.computeIfAbsent(key, e -> {
      try{
        Field field = getDeclaredFieldSuper(clazz, key);
        accessAndModifyHelper.setAccessible(field);
        return lookup.unreflectGetter(field);
      }catch(IllegalAccessException|NoSuchFieldException ex){
        throw new RuntimeException(ex);
      }
    });

    try{
      if(getter.type().parameterArray().length == 0){
        return (R) getter.invoke();
      }
      else{
        if(object == null)
          throw new NullPointerException("field " + key + " is not a static field, bug given object was null");
        return (R) getter.invoke(object);
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  public static <T> T getValueDefault(Object obj, String key){
    return (T) defaultHandlers.computeIfAbsent(obj.getClass(), e -> new FieldHandler(obj.getClass())).getValue(obj, key);
  }

  public static <T> T getValueDefault(Class<?> clazz, String key){
    return (T) defaultHandlers.computeIfAbsent(clazz, e -> new FieldHandler(clazz)).getValue(null, key);
  }

  public static <T> T getValueTemp(Object obj, String key){
    return (T) new FieldHandler(obj.getClass()).getValue(obj, key);
  }

  public static <T> T getValueTemp(Class<?> clazz, String key){
    return (T) new FieldHandler(clazz).getValue(null, key);
  }

  public static void clearDefault(){
    defaultHandlers.clear();
  }

  private void setFinalValueStatic(String key, Object value){
    Field field = finalFields.computeIfAbsent(key, e -> {
      try{
        return getDeclaredFieldSuper(clazz, key);
      }catch(NoSuchFieldException ex){
        throw new RuntimeException(ex);
      }
    });

    accessAndModifyHelper.setStatic(clazz, field, value);
  }

  private void setFinalValue(T target, String key, Object value){
    Field field = finalFields.computeIfAbsent(key, e -> {
      try{
        return getDeclaredFieldSuper(clazz, key);
      }catch(NoSuchFieldException ex){
        throw new RuntimeException(ex);
      }
    });

    accessAndModifyHelper.set(target, field, value);
  }
}
