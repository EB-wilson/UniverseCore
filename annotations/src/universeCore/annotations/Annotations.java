package universeCore.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**编译注解集，包含了一些编译时注解，用于mixin等*/
public class Annotations{
  /**UniverseCore导入语句，注解在mod的入口主类上，以在编译生成类时分配注册，预加载以及抽离加载时序
   * <p>参数requireVersion为最低要求universeCore版本的序列号，运行时若已安装版本低于此版本则不可用*/
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.SOURCE)
  public @interface ImportUNC{
    /**要求的最低前置版本序列号*/
    long requireVersion();
  }
  
  //-------------
  //MethodEntries
  //-------------
  /**注于接口实现的类之上，将自动检查实现的接口中的入口，并对接口中标记的入口方法加以对应实现，如果方法已经被实现则会跳过此方法
   * <p>传入一个布尔值作为参数，默认为true，决定是否允许默认入口检索，若为false则为严格模式，详细请参见各入口
   * @see BindField
   * @see MethodEntry*/
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.CLASS)
  public @interface ImplEntries{
    /**如果为真，则采取默认实现模式，为假则采取严格实现模式*/
    boolean value() default true;
  }
  
  //field
  /**为注解的字段分配一个键，用于匹配绑定到接口的字段入口，若实实现入口为非严格模式，且入口方法绑定的字段不存在，则会在实现类创建此字段*/
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface FieldKey{
    /**字段分配的键，用于标识绑定入口字段，在严格模式下，必须要求使用此注解分配字段键才可进行入口绑定*/
    String value();
  }
  
  /**使方法关联到一个字段，注于接口的方法上，方法应当是default的，否则会无法通过编译器上下文检查
   * <p>如果在实现类中没有对此方法的覆盖，则将实现类的此方法绑定到对应键的字段上，根据签名与参数区分getter/setter
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
   *     this.value = numb;
   *   }
   *
   *   @Override
   *   public int field(){
   *     return this.value;
   *   }
   *
   *   ......
   * }
   * }</pre>
   * 非严格模式下字段检索可以按继承关系和内部类包含关系向上搜索，检索选取最靠近实现类的对应字段
   * <p>严格模式下，字段检索将必须要求目标字段用@{@link FieldKey}注解标识字段的键，且只绑定到赋予了指定键的字段上，而不会创建和（或）绑定到检索键的同名字段
   * */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.CLASS)
  public @interface BindField{
    /**绑定到的字段，默认入口实现模式下，允许直接绑定到具有该名称的字段，严格模式下只会绑定到由{@link FieldKey}分配了相同键标识的字段*/
    String value();
  }
  
  //method
  /**将接口方法绑定到指定的入口方法，用方法名，参数列表选定入口方法，使用"->"将来源参数关联到本方法参数，不使用符号的参数则会被忽略，参数可以不指定，即为无参方法
   * <p>目标方法将自动调用此注解所在的方法并传入标记参数，调用顺序取决于接口实现的顺序以及方法在接口中的声明位置，注意，标记参数必须包含此方法的所有参数，且类型必须对应兼容
   * <pre>{@code
   * 示例：
   * public interface Sample{
   *   @MethodEntry(entryMethod = "test", paramType = {"java.lang.Object -> obj", "float"})
   *   default void entry(Object obj){
   *     System.out.println("testing" + obj);
   *   }
   * }
   *
   * @ImplEntries
   * public class Test implements Sample{
   *   public void test(Object obj){
   *
   *   }
   * }
   *
   * 则生成的Test类等价于:
   * @ImplEntries
   * public class Test implements Sample{
   *   public void test(Object obj, float value){
   *     Sample.super.entry(obj);
   *   }
   * }
   * }</pre>
   * 严格模式下，此注解要求所选择入口必须在目标类中已被声明，在超类或实现的接口中存在是不可被检索的
   * */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.CLASS)
  public @interface MethodEntry{
    /**入口绑定的方法名称，默认入口实现模式允许向超类搜索同名方法，严格模式下只能在实现类中搜索已存在的方法*/
    String entryMethod();
    /**入口方法的参数列表，用String全限定类名标识形式参数，并通过符号“->”将某参数传入到对应的入口类参数*/
    String[] paramTypes() default {};
  }
  
  //----------
  //entrust
  //----------
  /**注解于要进行委托的类上，编译后自动实现委托擦除abstract修饰符，指定委托进行的范围，由注解参数传入，参数遵循以下规则：
   * <p><strong>blackList</strong>: 默认为true，黑名单模式下，如果目标类上已经重写了某个方法，则委托会跳过这个方法，无论此方法是否具有实现
   * <p>反之在白名单模式下，委托只会应用在被声明了的方法上，这个方法应当为abstract或者具有空方法体，不能具有具体实现，否则同样会跳过
   * <p><strong>implement</strong>: 此委托的范围内要处理的接口，在类的implements中必须包含指定的所有接口，为空则不指定
   * <p><strong>extend</strong>: 委托类的范围，必须与类extends的类一致或者是其超类，类委托范围包含委托类的所有超类及其接口，接口多次委托只取一次，
   * 此参数值默认为java.lang.Object，为Object则不会进行类型分配与类方法委托（可认为不指定），方法只有为public且不为final或者static才可被委托
   * 但是传入的对象必须是注解指定的委托类的子类实例，同时其必须实现了所有委托指定的接口，否则是无法分配的
   * <pre>{@code 用例：
   * public class Sample{
   *   public void testFoo1(){}
   * }
   *
   * public interface Interface{
   *   void testFoo2();
   * }
   *
   * public class Ent extends Sample implements Interface{
   *   @Override
   *   public void testFoo1(){
   *     System.out.println("hello world");
   *   }
   *
   *   @Override
   *   public void testFoo2(){
   *     System.out.println("running");
   *   }
   * }
   *
   * @Entrust(implement = Interface.class, extend = Sample.class)
   * public abstract class Test extends Sample implements Interface{
   *   public Test(@EntrustInst Object source){}
   * }
   *
   * 生成的Test类等价于：
   * public class Test<$Type extends Ent & Interface> extends Sample implements Interface{
   *   private final $Type source;
   *
   *   public Test($Type source){
   *     this.source = source;
   *   }
   *
   *   public void testFoo1(){
   *     source.testFoo1();
   *   }
   *
   *   public void testFoo2(){
   *     source.testFoo2();
   *   }
   * }
   * }</pre>
   * 你需要用到{@link EntrustInst}注解来标注委托需要的一些特征，请参见此注解用法
   * @see EntrustInst
   * */
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.SOURCE)
  public @interface Entrust{
    /**如果为真，则为黑名单模式*/
    boolean blackList() default true;
    /**委托实现的接口列表，必须在类的implements语句中全部包含*/
    Class<?>[] implement() default {};
    /**委托的继承类型范围，必须是类的extends语句继承的类或是其超类（委托方法的范围不包含Object的方法）*/
    Class<?> extend() default Object.class;
  }
  
  /**用于标识委托描述符，一个委托类至少要有一个构造函数中包含一个被此注解标明的参数
   * <p><strong>用于构造函数参数时</strong>，此注解声明这个参数用于传入委托实例，具有一个boolean注解参数，用于决定此参数已经被主动分配，若为false，则在构造函数末尾生成赋值语句，将参数值赋予委托实例
   * <p><strong>用于字段时</strong>，最多声明一个字段，声明此字段将作为委托实例的存放字段，若不指定，则自动生成该字段用于委托处理
   * <p><strong>用于参数类型时</strong>，最多声明一个类型参数，此参数类型直接作为泛型参数，通常用于安全传入类型，指定的参数必须具备委托注解的extend目标类，生成时会填充implement指定的所有接口，
   * 若不指定，则自动生成为类型的参数，类型默认为类委托注解的extend类与所有接口
   * @see Entrust
   * */
  @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_PARAMETER})
  public @interface EntrustInst{
    /**仅作为构造函数参数时有效，表明是否已经手动分配，若为真，则不会生成构造函数的委托赋值语句*/
    boolean value() default false;
  }
}
