package universeCore.util.handler;

import arc.util.Log;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;

/**枚举处理器，提供了一些对enum的操作方法，可以创建枚举实例并将其放入枚举的values
 * <p>这个处理器是一个实例工厂，你需要对目标枚举类构造一个枚举处理器实例才可以进行操作
 * <pre>{@code 用例：
 * 由于桌面实现上的困难，目标枚举如果声明了构造器并且在其中有操作，那么你必须将构造函数对枚举实例的操作转化为lambda并传递给此操作实例的构造器参数
 *
 * 假设我们有枚举：
 * enum Test{
 *   a(125),
 *   b(368);
 *
 *   private final int data;
 *
 *   Test(int data){
 *     this.data = data;
 *   }
 * }
 * 这个枚举带有一个含参构造器，将data属性设为参数传入的int数据
 *
 * 那么对这个枚举的处理器应当如下声明：
 * EnumHandler<Test> handler = new EnumHandler<>(Test.class, (inst, param) -> {
 *   FieldHandler.setValue(inst, "data", param[0]);
 * });
 * }</pre>
 * 在构造枚举实例时会调用传入的这个lambda，并将新创建的枚举实例与构造函数参数传递给这个匿名函数
 * <p>由于属性是私有final，需要使用反射设置，此处引用了{@link FieldHandler}来完成反射操，在你得到枚举处理器的实例后即可直接引用处理器提供的方法
 * <p><strong>注意，鉴于mindustry源代码对枚举的声明，被操作的枚举可能带有一个数组保存所有枚举实例，但这仅仅是在初始化时调用了values()
 * <p>但这个数组是一个拷贝，如果你对枚举新增了项目，那么你最好将那个数组重新指定为变更后的values()</strong>
 * @author EBwilson
 * @since 1.0*/
@SuppressWarnings("unchecked")
public class EnumHandler<T extends Enum<?>>{
  private static final Unsafe unsafe;
  
  private final Class<T> clazz;
  private final BiConsumer<T, Object[]> constructor;
  
  static{
    Unsafe temp;
    try{
      Constructor<Unsafe> cstr = Unsafe.class.getDeclaredConstructor();
      cstr.setAccessible(true);
      temp = cstr.newInstance();
    }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
      Log.err(e);
      throw new RuntimeException(e);
    }
    unsafe = temp;
  }
  
  /**用目标枚举的类型与构造函数的外部实现构造一个枚举处理器，构造函数的外部实现效果应当与构造函数达到的效果保持一致，否则可能会产生意想不到的错误
   * @param clazz 处理器处理的目标枚举类型
   * @param cstr 构造函数的外部实现*/
  public EnumHandler(Class<T> clazz, BiConsumer<T, Object[]> cstr){
    this.clazz = clazz;
    this.constructor = cstr;
  }
  
  /**用目标枚举的类型构造一个没有构造函数实现的枚举处理器，如果枚举具有构造器实现，那么你必须提供构造函数的外部实现，否则可能会产生意想不到的错误
   * @param clazz 处理器处理的目标枚举类型*/
  public EnumHandler(Class<T> clazz){
    this.clazz = clazz;
    this.constructor = (e, p) -> {};
  }
  
  /**用指定的名称，枚举序数以及构造器参数来实例化一个枚举对象，
   * 如果你不将它放入枚举的子条目列表中，那么它的序数可以任意指定，尽管这可能引起不必要的错误
   * @param name 枚举的名字
   * @param ordinal 枚举的序数
   * @param param 传入给构造函数的参数
   * @return 一个具有指定名称与序数的枚举实例*/
  public T newEnumInstance(String name, int ordinal, Object... param){
    try{
      T result = (T)unsafe.allocateInstance(clazz);
  
      Field nameF = clazz.getSuperclass().getDeclaredField("name"), ordinalF = clazz.getSuperclass().getDeclaredField("ordinal");
  
      FieldHandler.setValue(nameF, result, name);
      FieldHandler.setValue(ordinalF, result, ordinal);
      
      constructor.accept(result, param);
      
      return result;
    }catch(InstantiationException | NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }
  
  /**创建一个枚举实例，并将其插入到枚举的末尾
   * @param addition 创建的实例的名称
   * @param param 附加的构造器参数列表*/
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
  
  /**创建一个枚举实例，并将其插入到该枚举项列表中的指定位置
   * 原位置的枚举实例以及其后方的实例都将后移，枚举序数随列表的更新同步更新
   * @param addition 创建的实例的名称
   * @param ordinal 插入的位置对应的序数
   * @param param 附加的构造器参数列表*/
  public T addEnumItem(String addition, int ordinal, Object... param){
    T newEnum = newEnumInstance(addition, ordinal, param);
    rearrange(newEnum, ordinal);
    return newEnum;
  }
  
  /**将指定枚举项放到指定序数的位置，并重设所有枚举项的序数到自己正确的位置
   * 枚举序数必须正确指定在0到枚举当前项目总数之间，新增项目的序数可以等于当前总数
   * @param instance 要插入的目标枚举实例
   * @param ordinal 要插入到的目标位置的枚举序数
   * @throws IndexOutOfBoundsException 如果指定的序数大于或等于枚举的当前元素总数，或者新增项目的序数大于元素总量*/
  public void rearrange(T instance, int ordinal){
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
      if(values.contains(instance) && ordinal >= values.size()) throw new IndexOutOfBoundsException("rearrange a exist item, ordinal should be less than amount of all items, (ordinal: " + ordinal + ", amount: " + values.size() + ")");
      else if(ordinal > values.size()) throw new IndexOutOfBoundsException("add a new item, ordinal should be equal or less than amount of all items, (ordinal: " + ordinal + ", amount: " + values.size() + ")");
  
      values.remove(instance);
      
      values.add(ordinal, instance);
      
      for(int i=0; i<values.size(); i++){
        FieldHandler.setValue(ordinalField, values.get(i), i);
      }
      FieldHandler.setValue(valuesField, null, values.toArray((T[]) Array.newInstance(clazz, 0)));
    }
    catch (SecurityException | IllegalArgumentException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
      throw new RuntimeException(e);
    }
  }
}
