package universeCore.util.handler;

import arc.util.Log;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public class EnumHandler<T extends Enum<?>>{
  private final Class<T> clazz;
  
  public EnumHandler(Class<T> clazz){
    this.clazz = clazz;
  }
  
  public T newEnumInstance(String name, int ordinal, Object... param){
    try{
      ArrayList<Class<?>> paramType = new ArrayList<>();
      ArrayList<Object> params = new ArrayList<>();
      
      paramType.add(String.class);
      paramType.add(int.class);
      
      params.add(name);
      params.add(ordinal);
      
      for(Object obj: param){
        paramType.add(asBasic(obj.getClass()));
        params.add(obj);
      }
  
      Constructor<T> cstr = clazz.getDeclaredConstructor(paramType.toArray(new Class[0]));
      cstr.setAccessible(true);
      return cstr.newInstance(params.toArray(new Object[0]));
    }catch(InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
      throw new RuntimeException(e);
    }
  }
  
  private static Class<?> asBasic(Class<?> clazz){
    if(clazz == Integer.class) return int.class;
    if(clazz == Float.class) return float.class;
    if(clazz == Double.class) return double.class;
    if(clazz == Long.class) return long.class;
    if(clazz == Boolean.class) return boolean.class;
    if(clazz == Short.class) return short.class;
    if(clazz == Byte.class) return byte.class;
    return clazz;
  }
  
  public T addEnumItemTail(String addition, Object... param){
    try{
      Method method = clazz.getMethod("values");
      method.setAccessible(true);
      return addEnumItem(addition, ((Object[])method.invoke(null)).length, param);
    }
    catch(SecurityException | NoSuchMethodException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }
  
  /**用指定的枚举类型创建一个枚举实例，并将其添加到该枚举项列表中的指定位置
   * 原位置的常量以及其后方的常量都将后移，枚举序数随列表的更新同步更新
   * @param addition 创建的实例的名称
   * @param ordinal 插入的位置对应的序数
   * @param param 附加的构造器参数列表*/
  public T addEnumItem(String addition, int ordinal, Object... param){
    T newEnum = newEnumInstance(addition, ordinal, param);
    rearrange(newEnum, ordinal);
    return newEnum;
  }
  
  public void rearrange(T object, int ordinal){
    Field valuesField = null;
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (field.getName().contains("$VALUES")) {
        valuesField = field;
        break;
      }
    }
  
    try{
      assert valuesField != null;
      Field ordinalField = clazz.getSuperclass().getDeclaredField("ordinal");
      Method method = clazz.getMethod("values");
      method.setAccessible(true);
      T[] arr = (T[])method.invoke(null);
      ArrayList<T> values = new ArrayList<>(Arrays.asList(arr));
  
      values.remove(object);
      
      values.add(ordinal, object);
      
      for(int i=0; i<values.size(); i++){
        FieldHandler.setValue(ordinalField, values.get(i), i);
      }
      FieldHandler.setValue(valuesField, null, values.toArray((T[]) Array.newInstance(clazz, 0)));
    }
    catch (SecurityException | IllegalArgumentException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
      throw new RuntimeException(e);
    }
  }
  
  public void constructor(T instance, Object... param){}
}
