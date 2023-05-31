package dynamilize;

import dynamilize.annotation.Exclude;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**保存动态对象行为信息的动态类型，描述了对象的共有行为和变量信息。
 * <p>在{@link DynamicMaker}的构造实例方法里使用动态类型构造动态对象，动态对象会具有其类型描述的行为，对于基类与动态类中描述的同一方法会正常的处理覆盖关系。
 * <p>不同于为对象设置的函数，在动态类中描述的方法，对于所有此类的实例都是共用的，即：
 * <ul>
 * <li><strong>当对象的动态类型的某一方法发生变更时，对象的此方法行为也会改变，且变更会即时生效</strong>
 * <li><strong>当动态类的超类的方法行为发生变更时，若类的行为中引用了超类方法，则类的行为会随之改变</strong>
 * <p><strong>仅在动态对象的方法来自动态类描述的行为时，上述变更才会生效</strong>
 * </ul>
 * <p>描述动态类的行为需要{@linkplain DynamicClass#visitClass(Class,JavaHandleHelper) 行为样版}或者函数表达式，以增量模式编辑类型行为，方法和默认变量只能新增/变更，不可删除
 * <pre>{@code
 * 下面是一个简单的样例:
 * public class Template{
 *   public static String str = "string0";
 *
 *   public static void method(@This final DynamicObject self, String arg0){
 *     System.out.println(self.getVar(arg0));
 *   }
 * }
 *
 * 引用：
 * DynamicMaker maker = DynamicMaker.getDefault();
 * DynamicClass dyClass = DynamicClass.get("Sample");
 * dyClass.visitClass(Template.class, maker.getHelper());
 * DynamicObject dyObject = maker.newInstance(dyClass);
 * dyObject.invokeFunc("method", "str");
 *
 * >>> string0
 * }</pre>
 *
 * <strong>动态类型声明的变量初始值只在对象被创建时有效，任何时候改变类的变量初始值都不会对已有实例造成影响</strong>
 *
 * @see DynamicMaker#newInstance(Class, Class[], DynamicClass, Object...)
 * @see DynamicMaker#newInstance(DynamicClass)
 * @see DynamicMaker#newInstance(Class[], DynamicClass)
 * @see DynamicMaker#newInstance(Class, DynamicClass, Object...)
 *
 * @author EBwilson */
public class DynamicClass{
  /**保存了所有动态类的实例，通常情况下动态类型只有在主动删除时才会退出池，对于废弃的类，请切记使用{@link DynamicClass#delete()}删除，否则会造成内存泄漏*/
  private static final HashMap<String, DynamicClass> classPool = new HashMap<>();

  /**类型的唯一限定名称*/
  private final String name;

  /**此动态类的直接超类*/
  private final DynamicClass superDyClass;

  private final DataPool data;
  private final Map<String, Initializer<?>> varInit = new HashMap<>();

  /**废弃标记，在类型已废弃后，不可再实例化此类型*/
  private boolean isObsoleted;

  /**声明一个动态类型，如果此名称指明的类型不存在则使用给出的名称创建一个新的动态类
   *
   * @param name 类型的唯一限定名称
   * @param superDyClass 此动态类型的直接超类
   * @return 一个具有指定名称的动态类实例
   *
   * @throws IllegalHandleException 如果具有 该名称的类型已经存在*/
  public static DynamicClass declare(String name, DynamicClass superDyClass){
    if(classPool.containsKey(name))
      throw new IllegalHandleException("cannot declare two dynamic class with same name");

    DynamicClass dyc = new DynamicClass(name, superDyClass);
    classPool.put(name, dyc);
    return dyc;
  }

  /**获取动态类实例，如果此名称指明的类型不存在则使用给出的名称创建一个新的动态类
   * <p>从此方法创建的新类没有明确的直接超类，实例将以委托的基类作为直接超类，若需要具有明确的直接超类的类型，请使用{@link DynamicClass#declare(String, DynamicClass)}声明
   *
   * @param name 类型的唯一限定名称
   * @return 一个具有指定名称的动态类实例*/
  public static DynamicClass get(String name){
    return classPool.computeIfAbsent(name, n -> new DynamicClass(n, null));
  }

  /**创建类型实例，不应从外部调用此方法构造实例*/
  private DynamicClass(String name, DynamicClass superDyClass){
    this.name = name;
    this.superDyClass = superDyClass;
    this.data = new DataPool(superDyClass == null? null: superDyClass.data);
  }

  /**将此类型对象从池中移除并废弃，任何一个动态类不再被使用后，都应当正确的删除。
   * <p>在你调用此方法之前，<strong>请确保已经没有任何对此类型的引用</strong>*/
  public void delete(){
    checkFinalized();

    classPool.remove(name);
    isObsoleted = true;
  }

  /**获取此动态类型的名称
   *
   * @return 类型的唯一限定名称*/
  public String getName(){
    checkFinalized();

    return name;
  }

  /**获取此动态类型的直接超类，可能为空，为空时表明此类的实例以委托类型作为直接超类
   *
   * @return 类型的直接超类*/
  public DynamicClass superDyClass(){
    checkFinalized();

    return superDyClass;
  }

  public Map<String, Initializer<?>> getVarInit(){
    return varInit;
  }

  public DataPool genPool(DataPool basePool){
    return new DataPool(data){
      @Override
      public IFunctionEntry select(String name1, FunctionType type){
        IFunctionEntry res1 = super.select(name1, type);
        if(res1 != null) return res1;

        return basePool.select(name1, type);
      }

      @Override
      public IVariable getVariable(String name1){
        IVariable var = super.getVariable(name1);
        if(var != null) return var;

        return basePool.getVariable(name1);
      }
    };
  }

  public IFunctionEntry[] getFunctions(){
    return data.getFunctions();
  }

  public IVariable[] getVariables(){
    return data.getVariables();
  }

  /**访问一个类作为行为样版，将类中声明的字段/方法用作描述动态类行为，类中声明的<strong>静态成员</strong>将产生如下效果:
   * <ul>
   * <li><strong>方法</strong>：为动态类型描述实例共有方法，对于同名同参数的方法若重复传入，则旧的方法会被新的覆盖。
   * <p>方法样版会创建为方法入口，当实例的此方法被调用时实际上调用会被转入这个样版方法，this指针会作为一个参数被传递（可选）
   * <p>要接收this指针，你需要使用{@link dynamilize.annotation.This}注解标记样版方法的第一个参数且参数应当为final，
   * 被标记为this指针的参数类型必须为可分配类型（动态实例可确保已实现了接口DynamicObject），此参数不会占据参数表匹配位置：
   * <p>例如方法<pre>{@code sample(@This final DynamicObject self, String str)}</pre>可以正确的匹配到对象的函数<pre>{@code sample(String str)}</pre>
   * 若方法带有final修饰符（尽管这可能会让你收到IDE环境的警告），则此方法在类的行为中将变得不可变更，但是对于对象的此函数依然可以正常替换
   * <p><strong>仅有被替换的方法可以改变实例的行为，对于新增的行为将不会影响已存在的实例的行为，新增行为只会使新产生的实例具有此默认函数</strong>
   *
   * <li><strong>字段</strong>：为动态类型描述默认变量表，并以字段的当前值作为函数的默认初始化数值。
   * <p>若字段的类型为{@link dynamilize.Initializer.Producer}，则会将此函数作为值的工厂，初始化动态实例时以函数生产的数据作为变量默认值。
   * <p>如果字段携带final修饰符，那么此变量将被标记为常量，不可变更。
   * <p><strong>除作为行为样版被访问之外，其他任何时机变量的值变化都不会对类型的行为产生直接影响</strong>
   * </ul>
   * 如果模板里存在不希望被作为样版的字段或者方法，你可以使用{@link Exclude}注解标记此目标以排除。
   * <p><strong>所有描述动态类行为的方法和变量都必须具有public static修饰符</strong>*/
  public void visitClass(Class<?> template, JavaHandleHelper helper){
    checkFinalized();

    for(Method method: template.getDeclaredMethods()){
      if(method.getAnnotation(Exclude.class) != null) continue;

      if(!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) continue;

      helper.makeAccess(method);
      data.setFunction(helper.genJavaMethodRef(method, data));
    }

    for(Field field: template.getDeclaredFields()){
      if(field.getAnnotation(Exclude.class) != null) continue;

      if(!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers())) continue;

      setVariableWithField(field);
    }
  }

  /**访问一个方法样版，不同于{@link DynamicClass#visitClass(Class,JavaHandleHelper)}，此方法只访问一个单独的方法并创建其行为样版。
   * <p>关于此方法的具体行为，请参阅访问行为样版类型的{@linkplain  DynamicClass#visitClass(Class,JavaHandleHelper) 方法部分}
   *
   * @param method 访问的方法样版*/
  public void visitMethod(Method method, JavaHandleHelper helper){
    if(!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers()))
      throw new IllegalHandleException("method template must be public and static");

    helper.makeAccess(method);
    data.setFunction(helper.genJavaMethodRef(method, data));
  }

  /**访问一个字段样版，不同于{@link DynamicClass#visitClass(Class,JavaHandleHelper)}，此方法只访问一个单独的字段并创建其行为样版。
   * <p>关于此方法的具体行为，请参阅访问行为样版类型的{@linkplain  DynamicClass#visitClass(Class,JavaHandleHelper) 字段部分}
   *
   * @param field 访问的字段样版*/
  public void visitField(Field field){
    if(!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers()))
      throw new IllegalHandleException("field template must be public and static");

    setVariableWithField(field);
  }

  /**以lambda模式设置函数，使用匿名函数描述类型行为，对类型行为变更的效果与{@link DynamicClass#visitClass(Class,JavaHandleHelper)}的方法部分相同
   *
   * @param name 函数名称
   * @param func 描述函数行为的匿名函数
   * @param argTypes 函数的形式参数类型*/
  public <S, R> void setFunction(String name, Function<S, R> func, Class<?>... argTypes){
    data.setFunction(name, func, argTypes);
  }

  public <S, R> void setFunction(String name, Function.SuperGetFunction<S, R> func, Class<?>... argTypes){
    data.setFunction(name, func, argTypes);
  }

  /**同{@link DynamicClass#setFunction(String, Function, Class[])}，只是匿名函数无返回值*/
  public <S> void setFunction(String name, Function.NonRetFunction<S> func, Class<?>... argTypes){
    this.<S, Object>setFunction(name, (s, a) -> {
      func.invoke(s, a);
      return null;
    }, argTypes);
  }

  /**同{@link DynamicClass#setFunction(String, Function.SuperGetFunction, Class[])}，只是匿名函数无返回值*/
  public <S> void setFunction(String name, Function.NonRetSuperGetFunc<S> func, Class<?>... argTypes){
    this.<S, Object>setFunction(name, (s, sup, a) -> {
      func.invoke(s, sup, a);
      return null;
    }, argTypes);
  }

  /**常量模式设置变量初始值，行为与{@link DynamicClass#visitClass(Class,JavaHandleHelper)}字段部分相同
   *
   * @param name 变量名称
   * @param value 常量值
   * @param isConst 此变量是否是一个不可变常量*/
  public void setVariable(String name, Object value, boolean isConst){
    setVariable(name, () -> value, isConst);
  }

  /**函数模式设置变量初始化工厂，行为与{@link DynamicClass#visitClass(Class,JavaHandleHelper)}字段部分相同
   *
   * @param name 变量名称
   * @param prov 生产变量初始值的工厂函数
   * @param isConst 此变量是否是一个不可变常量*/
  public void setVariable(String name, Initializer.Producer<?> prov, boolean isConst){
    varInit.put(name, new Initializer<>(prov, isConst));
  }

  @SuppressWarnings({"unchecked"})
  private void setVariableWithField(Field field){
    Object value;
    try{
      value = field.get(null);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }

    varInit.put(field.getName(), new Initializer<>(value instanceof Initializer.Producer? (Initializer.Producer<? super Object>) value: () -> value,
        Modifier.isFinal(field.getModifiers())));
  }

  private void checkFinalized(){
    if(isObsoleted)
      throw new IllegalHandleException("cannot do anything on obsoleted dynamic class");
  }

  @Override
  public String toString(){
    return "dynamic class:" + name;
  }
}
