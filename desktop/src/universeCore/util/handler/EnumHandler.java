package universeCore.util.handler;

import arc.util.Log;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public class EnumHandler<T extends Enum<?>>{
  private static final Unsafe unsafe;
  
  private final Class<T> clazz;
  
  static{
    Unsafe temp;
    try{
      Constructor<Unsafe> cstr = Unsafe.class.getDeclaredConstructor();
      cstr.setAccessible(true);
      temp = cstr.newInstance();
    }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
      temp = null;
      Log.err(e);
    }
    unsafe = temp;
  }
  
  public EnumHandler(Class<T> clazz){
    this.clazz = clazz;
  }
  
  public T newEnumInstance(String name, int ordinal, Object... param){
    try{
      T result = (T)unsafe.allocateInstance(clazz);
  
      Field nameF = clazz.getSuperclass().getDeclaredField("name"), ordinalF = clazz.getSuperclass().getDeclaredField("ordinal");
  
      FieldHandler.setValue(nameF, result, name);
      FieldHandler.setValue(ordinalF, result, ordinal);
      
      constructor(result, param);
      
      return result;
    }catch(InstantiationException | NoSuchFieldException e){
      Log.err(e);
      return null;
    }
  }
  
  public T addEnumItemTail(String addition, Object... param){
    try{
      Method method = clazz.getMethod("values");
      method.setAccessible(true);
      return addEnumItem(addition, ((Object[])method.invoke(null)).length, param);
    }
    catch(SecurityException | NoSuchMethodException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e){
      Log.err(e);
      System.out.println(e.toString());
      return null;
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
      Log.err(e);
    }
  }
  
  public void constructor(T instance, Object... param){}
}
