package universeCore.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Annotations{
  /**UniverseCore导入语句，注解在mod的入口主类上，以在编译生成类时分配注册，预加载以及抽离加载时序，以保证mod能够正常运行
   * </p>参数requireVersion为最低要求universeCore版本的序列号，运行时若低于此版本则不可用*/
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.SOURCE)
  public @interface ImportUNC{
    long requireVersion();
  }
  
  //-------------
  //MethodEntries
  //-------------
  /**注于接口实现的类之上，将自动检查实现的接口中的入口，并对接口中标记的入口方法加以默认实现
   * <p>传入一个布尔值作为参数，决定是否为默认实现*/
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.SOURCE)
  public @interface ImplEntries{
    boolean value();
  }
  
  //field
  /**注于接口的方法上，方法应当是default的，否则会无法通过编译器上下文检查
   * </p>如果在实现类中没有对此方法的覆盖，则将实现类的此方法绑定到对应键的字段上，根据签名与参数区分getter/setter
   * <pre>{@code 例如：
   * public interface Sample{
   *   @BindField("field")
   *   default void field(int numb){} //setter
   *
   *   @BindField("numb")
   *   default int field(){
   *     return 0; //getter
   *   }
   * }
   * }</pre>
   *
   * 编译后具有{@link ImplEntries}注解的此接口实现类，若上述两方法未进行实现，则生成的代码等价于：
   * <pre>{@code
   * @ImplEntries
   * public class Test implements Sample{
   *   ......
   *
   *   @FieldKey("field")
   *   private int value;
   *
   *   @Override
   *   public void field(int numb){
   *     value = numb;
   *   }
   *
   *   @Override
   *   public int field(){
   *     return value;
   *   }
   *
   *   ......
   * }
   * }</pre>
   * */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface BindField{
    String value();
  }
  
  /**为注解的字段分配一个键，用于匹配绑定到接口的字段入口，若为默认实现且绑定的字段不存在，则会在入口类创建此字段*/
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface FieldKey{
    String value();
  }
}
