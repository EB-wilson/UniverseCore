package dynamilize;

import java.util.*;

/**用于存储和处置动态对象数据的信息容器，不应从外部访问，每一个动态对象都会绑定一个数据池存放对象的变量/函数等信息。
 * <p>对于一个{@linkplain DynamicClass 动态类}的实例，实例的数据池一定会有一个父池，这个池以动态类的直接超类描述的信息进行初始化。
 * <p>访问池信息无论如何都是以最近原则，即若本池内没有找到数据，则以距离实例的池最近的具有此变量/函数的父池的数据为准
 *
 * @author EBwilson*/
@SuppressWarnings({"unchecked"})
public class DataPool{
  private static final String init = "<init>";

  private static final List<IFunctionEntry> TMP_LIS = new ArrayList<>();
  public static final IFunctionEntry[] EMP_METS = new IFunctionEntry[0];

  private static final List<IVariable> TMP_VAR = new ArrayList<>();
  public static final IVariable[] EMP_VARS = new IVariable[0];

  private final DataPool superPool;

  private final Map<String, Map<FunctionType, IFunctionEntry>> funcPool = new HashMap<>();
  private final Map<String, IVariable> varPool = new HashMap<>();

  /**创建一个池对象并绑定到父池，父池可为null，这种情况下此池应当为被委托类型的方法/字段引用。
   * <p><strong>你不应该在外部使用时调用此类型</strong>
   *
   * @param superPool 此池的父池*/
  public DataPool(DataPool superPool){
    this.superPool = superPool;
  }

  public void init(DynamicObject<?> self, Object... args){
    DynamicClass curr = self.getDyClass();
    HashSet<String> varSetted = new HashSet<>();

    while(curr != null){
      for(Map.Entry<String, Initializer<?>> entry: curr.getVarInit().entrySet()){
        if(varSetted.add(entry.getKey())) self.setVar(entry.getKey(), entry.getValue());
      }

      curr = curr.superDyClass();
    }

    ArgumentList lis = ArgumentList.as(args);
    IFunctionEntry fun = select(init, lis.type());
    if(fun == null) return;

    fun.getFunction().invoke((DynamicObject<Object>) self, args);
    lis.type().recycle();
    lis.recycle();
  }

  public void setConstructor(Function<?, ?> function, Class<?>... argType){
    setFunction(init, function, argType);
  }

  public Function<?, ?> getConstructor(Class<?>... argType){
    FunctionType type = FunctionType.inst(argType);
    Function<?, ?> fun = select(init, type).getFunction();
    type.recycle();
    return fun;
  }

  /**在本池设置一个函数，无论父池是否具有同名同类型的函数存在，若本池中存在同名同类型函数，则旧函数将被覆盖。
   * <p>函数的名称，行为与参数类型和java方法保持一致，返回值由行为传入的匿名函数确定，例如：
   * <pre>{@code
   * java定义的方法
   * int getTime(String location){
   *   return foo(location);
   * }
   * 等价于设置函数
   * set("getTime", (self, args) -> return foo(args.get(0)), String.class);
   * }</pre>
   *
   * @param name 函数名称
   * @param argsType 函数的参数类型列表
   * @param function 描述此函数行为的匿名函数*/
  public void setFunction(String name, Function<?, ?> function, Class<?>... argsType){
    FunctionType type = FunctionType.inst(argsType);
    funcPool.computeIfAbsent(name, n -> new HashMap<>())
        .put(type, new FunctionEntry<>(name, true, function, type, this));
  }

  public <R, S> void setFunction(String name, Function.SuperGetFunction<S,R> func, Class<?>[] argTypes){
    FunctionType type = FunctionType.inst(argTypes);
    funcPool.computeIfAbsent(name, n -> new HashMap<>())
        .put(type, new FunctionEntry<>(name, true, func, type, this));
  }

  public void setFunction(IFunctionEntry functionEntry){
    if(functionEntry.owner() != this)
      throw new IllegalHandleException("function owner pool must equal added pool");

    funcPool.computeIfAbsent(functionEntry.getName(), e -> new HashMap<>()).put(functionEntry.getType(), functionEntry);
  }

  /**从类层次结构中获取变量的对象
   *
   * @param name 变量名
   * @return 变量对象*/
  public IVariable getVariable(String name){
    IVariable var = varPool.get(name);
    if(var != null) return var;

    if(superPool != null){
      return superPool.getVariable(name);
    }

    return null;
  }

  /**向池中添加一个变量对象，一般来说仅在标记java字段作为变量时会用到
   *
   * @param var 加入池的变量*/
  public void setVariable(IVariable var){
    varPool.putIfAbsent(var.name(), var);
  }

  /**将类层次结构中定义的函数输出为函数入口，会优先查找类型签名相同的函数，若未查找到相同的才会转入类型签名匹配的函数，
   * 因此调用函数在性能需求较高的情况下，建议对实参列表明确声明类型的签名，这可以有效提高重载决策的速度
   * <p>如果函数没有被定义则返回空
   *
   * @param name 函数的名称
   * @param type 函数的参数类型
   * @return 选中函数的函数入口*/
  public IFunctionEntry select(String name, FunctionType type){
    Map<FunctionType, IFunctionEntry> map;
    IFunctionEntry res;

    DataPool curr = this;
    while(curr != null){
      map = curr.funcPool.get(name);
      if(map != null){
        res = map.get(type);
        if(res != null) return res;
      }

      curr = curr.superPool;
    }

    curr = this;
    while(curr != null){
      map = curr.funcPool.get(name);
      if(map != null){
        for(Map.Entry<FunctionType, IFunctionEntry> entry: map.entrySet()){
          if(entry.getKey().match(type.getTypes())){
            return entry.getValue();
          }
        }
      }

      curr = curr.superPool;
    }

    return null;
  }

  public IVariable[] getVariables(){
    TMP_VAR.clear();
    TMP_VAR.addAll(varPool.values());
    return TMP_VAR.toArray(EMP_VARS);
  }

  public IFunctionEntry[] getFunctions(){
    TMP_LIS.clear();
    for(Map<FunctionType, IFunctionEntry> entry: funcPool.values()){
      TMP_LIS.addAll(entry.values());
    }

    return TMP_LIS.toArray(EMP_METS);
  }

  /**获得池的只读对象*/
  public <S> ReadOnlyPool getReader(DynamicObject<S> owner){
    return new ReadOnlyPool(this, owner, null);
  }

  public <T> ReadOnlyPool getSuper(DynamicObject<T> owner, ReadOnlyPool alternative){
    return superPool == null? alternative: ReadOnlyPool.get(superPool, owner, alternative);
  }

  public static class ReadOnlyPool{
    public static int MAX_CHANCES = 2048;
    private static final Stack<ReadOnlyPool> POOLS = new Stack<>();

    private DataPool pool;
    private DynamicObject<?> owner;
    private ReadOnlyPool alternative;

    private final boolean hold;

    private ReadOnlyPool(){
      hold = false;
    }

    private ReadOnlyPool(DataPool pool, DynamicObject<?> owner, ReadOnlyPool alternative){
      this.pool = pool;
      this.owner = owner;
      this.alternative = alternative;

      hold = true;
    }

    private static ReadOnlyPool get(DataPool source, DynamicObject<?> owner, ReadOnlyPool alternative){
      ReadOnlyPool res = POOLS.isEmpty()? new ReadOnlyPool(): POOLS.pop();
      res.pool = source;
      res.owner = owner;
      res.alternative = alternative;

      return res;
    }

    public void recycle(){
      if(hold) return;

      pool = null;
      owner = null;
      alternative = null;

      if(POOLS.size() < MAX_CHANCES) POOLS.push(this);
    }

    /**@see DynamicObject#getVar(String)*/
    public <T> T getVar(String name){
      IVariable var = pool.getVariable(name);

      if(var == null) var = alternative == null? null: alternative.getVar(name);

      if(var == null)
        throw new IllegalHandleException("variable " + name + " was not definer");

      return var.get(owner);
    }

    /**@see DynamicObject#getFunc(String, FunctionType)*/
    public IFunctionEntry getFunc(String name, FunctionType type){
      IFunctionEntry func = pool.select(name, type);
      if(func == null){
        if(alternative == null)
          throw new IllegalHandleException("no such function: " + name + type);

        return alternative.getFunc(name, type);
      }

      return func;
    }

    /**@see DynamicObject#invokeFunc(String, Object...)*/
    public <R> R invokeFunc(String name, Object... args){
      ArgumentList lis = ArgumentList.as(args);
      R r = invokeFunc(name, lis);
      lis.type().recycle();
      lis.recycle();
      return r;
    }

    public <R> R invokeFunc(FunctionType type, String name, Object... args){
      ArgumentList lis = ArgumentList.asWithType(type, args);
      R r = invokeFunc(name, lis);
      lis.recycle();
      return r;
    }

    /**@see DynamicObject#invokeFunc(String, ArgumentList)*/
    public <R> R invokeFunc(String name, ArgumentList args){
      FunctionType type = args.type();

      return (R) getFunc(name, type).getFunction().invoke((DynamicObject<Object>) owner, args);
    }
  }
}
