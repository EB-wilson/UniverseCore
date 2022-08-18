package dynamilize;

/**所有动态对象依赖的接口，描述了动态对象具有的基本行为，关于接口的实现应当由生成器生成。
 * <p>实现此接口通常不应该从外部进行，而应当通过{@link DynamicMaker#makeClassInfo(Class, Class[])}生成，对于生成器生成的实现类应当满足下列行为：
 * <ul>
 * <li>分配对象保存{@linkplain DataPool 数据池}的字段，字段具有private final修饰符
 * <li>分配对象保存{@linkplain DynamicClass 动态类}的字段，字段具有private final修饰符
 * <li>对每一个超类构造函数生成相应的构造函数，并正确的调用超类的相应超类构造函数
 * 参数前新增两个参数分别传入{@linkplain DataPool 数据池}和{@linkplain DynamicClass 动态类}并分配给成员字段
 * <li>按此接口内的方法说明，实现接口的各抽象方法
 * </ul>
 * 且委托类型的构造函数应当从{@link DynamicMaker}中的几个newInstance调用，否则你必须为之提供合适的构造函数参数
 *
 * @author EBwilson */
@SuppressWarnings("unchecked")
public interface DynamicObject<Self>{
  /**获取对象的动态类型
   * <p>生成器实施应当实现此方法返回生成的动态类型字段
   *
   * @return 此对象的动态类型*/
  DynamicClass getDyClass();

  /**获得对象的成员变量
   * <p>生成器实施应当实现此方法使之调用数据池的{@link DataPool#getVariable(String)}方法并返回值
   *
   * @param name 变量名称
   * @return 变量的值*/
  IVariable getVariable(String name);

  DataPool.ReadOnlyPool baseSuperPointer();

  /**设置对象的成员变量
   * <p>生成器实施应当实现此方法使之调用数据池的{@link DataPool#setVariable(IVariable)}方法，自身的参数分别传入
   *
   * @param variable 将设置的变量*/
  void setVariable(IVariable variable);

  /**获取对象的某一成员变量的值，若变量尚未定义则会抛出异常
   *
   * @param name 变量名
   * @return 变量值*/
  default <T> T getVar(String name){
    IVariable var = getVariable(name);
    if(var == null)
      throw new IllegalHandleException("variable " + name + " was not defined");

    return var.get(this);
  }

  /**为对象的某一变量设置属性值，若在层次结构中未能找到变量则会定义变量
   *
   * @param name 变量名
   * @param value 属性值*/
  default <T> void setVar(String name, T value){
    IVariable var = getVariable(name);
    if(var == null){
      var = new Variable(name, false);
      setVariable(var);
    }
    var.set(this, value);
  }

  <T> T varValueGet(String name);

  <T> void varValueSet(String name, T value);

  /**使用给出的运算器对指定名称的变量进行处理，并用其计算结果设置变量值
   *
   * @param name 变量名称
   * @param calculator 计算器
   * @return 计算结果*/
  default <T> T calculateVar(String name, Calculator<T> calculator){
    T res;
    setVar(name, res = calculator.calculate(getVar(name)));
    return res;
  }

  /**获取对象的函数的匿名函数表示
   * <p>生成器实施应当实现此方法使之调用数据池的{@link DataPool#select(String, FunctionType)}方法并返回值
   *
   * @param name 函数名称
   * @param type 函数的参数类型
   * @return 指定函数的匿名表示*/
  IFunctionEntry getFunc(String name, FunctionType type);

  default <R> Func<R> getFunction(String name, FunctionType type){
    IFunctionEntry entry = getFunc(name, type);
    if(entry == null)
      throw new IllegalHandleException("no such function: " + name + type);

    return a -> (R) entry.<Self, R>getFunction().invoke(this, a);
  }

  default <R> Function<Self, R> getFunc(String name, Class<?>... types){
    FunctionType type = FunctionType.inst(types);
    Function<Self, R> f = getFunc(name, type).getFunction();
    type.recycle();
    return f;
  }

  default <R> Func<R> getFunction(String name, Class<?>... types){
    FunctionType type = FunctionType.inst(types);
    Func<R> f = getFunction(name, type);
    type.recycle();
    return f;
  }

  /**以lambda模式设置对象的成员函数，lambda模式下对对象的函数变更仅对此对象有效，变更即时生效,
   * 若需要使变更对所有实例都生效，则应当对此对象的动态类型引用{@link DynamicClass#visitClass(Class)}方法变更行为样版
   * <p>生成器实施应当实现此方法使之调用数据池的{@link DataPool#setFunction(String, Function, Class[])}方法，并将参数一一对应传入
   * <p><strong>注意，含有泛型的参数，无论类型参数如何，形式参数类型始终为{@link Object}</strong>
   *
   * @param name 设置的函数名称
   * @param func 描述函数行为的匿名函数
   * @param argTypes 形式参数的类型列表*/
  <R> void setFunc(String name, Function<Self, R> func, Class<?>... argTypes);

  <R> void setFunc(String name, Function.SuperGetFunction<Self, R> func, Class<?>... argTypes);

  /**与{@link DynamicObject#setFunc(String, Function, Class[])}效果一致，只是传入的函数没有返回值*/
  default void setFunc(String name, Function.NonRetFunction<Self> func, Class<?>... argTypes){
    setFunc(name, (s, a) -> {
      func.invoke(s, a);
      return null;
    }, argTypes);
  }

  /**与{@link DynamicObject#setFunc(String, Function.SuperGetFunction, Class[])}效果一致，只是传入的函数没有返回值*/
  default void setFunc(String name, Function.NonRetSuperGetFunc<Self> func, Class<?>... argTypes){
    setFunc(name, (s, sup, a) -> {
      func.invoke(s, sup, a);
      return null;
    }, argTypes);
  }

  /**执行对象的指定成员函数
   *
   * @param name 函数名称
   * @param args 传递给函数的实参列表
   * @return 函数返回值*/
  default <R> R invokeFunc(String name, Object... args){
    ArgumentList lis = ArgumentList.as(args);
    R r = invokeFunc(name, lis);
    lis.type().recycle();
    lis.recycle();
    return r;
  }
  
  /**指明形式参数列表执行对象的指定成员函数，如果参数中有从类型分配的对象或者null值，使用type明确指定形参类型可以有效提升执行效率
   *
   * @param name 函数名称
   * @param args 传递给函数的实参列表
   * @return 函数返回值*/
  default <R> R invokeFunc(FunctionType type, String name, Object... args){
    ArgumentList lis = ArgumentList.asWithType(type, args);
    R r = invokeFunc(name, lis);
    lis.recycle();
    return r;
  }

  /**直接传入{@link ArgumentList}作为实参列表的函数调用，方法内完成拆箱，便于在匿名函数中对另一函数进行引用而无需拆箱
   *
   * @param name 函数名称
   * @param args 是按列表的封装对象
   * @return 函数返回值*/
  default <R> R invokeFunc(String name, ArgumentList args){
    FunctionType type = args.type();
    Function<Self, R> res = getFunc(name, type).getFunction();

    if(res == null)
      throw new IllegalHandleException("no such method declared: " + name);

    return res.invoke( this, args);
  }

  default <T extends Self> T self(){
    return (T) this;
  }
}
