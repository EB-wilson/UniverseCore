package dynamilize;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**代理创建工具，用于生成类似{@linkplain  java.lang.reflect.Proxy java代理工具}的面向切面代理实例，但不同的是这允许从类型进行委托，类似于<i>cglib</i>。
 * <p>通过此工具创建的代理实例会将所有可用（非static/final/private/package private）方法调用转入代理调用处理器，在此工具中被声明为了{@link ProxyMaker#invoke(DynamicObject, Caller, ArgumentList)}])}。
 * <p>使用此工具时需要给出一个{@link DynamicMaker}用于构建动态代理实例，尽管可能你需要的只是具备代理行为的{@linkplain DynamicClass 动态类型}。
 * <p>另外，动态类型中声明的函数也会被代理拦截，作用时机与动态类实例化的行为影响是一致的，请参阅{@link DynamicClass 动态类型函数变更的作用时机}
 * <p>你可以使用lambda表达式引用{@link ProxyMaker#getDefault(DynamicMaker, ProxyHandler)}获取默认的lambda实现，
 * 或者实现此类的抽象方法{@link ProxyMaker#invoke(DynamicObject, Caller, ArgumentList)}。
 * <pre>{@code
 * 一个简单的用例：
 * ArrayList list = ProxyMaker.getDefault(DynamicMaker.getDefault(), (self, method, args) -> {
 *   System.out.println(method);
 *   return method.invoke(self, args);
 * }).newProxyInstance(ArrayList.class).self();
 *
 * 这个list的所有可用方法在执行时都将会打印方法本身的签名
 * }</pre>
 *
 * @since 1.2
 * @author EBwilson */
public abstract class ProxyMaker{
  private static final Map<DynamicClass, Map<ClassImplements<?>, DynamicClass>> proxyMap = new WeakHashMap<>();
  private static final Map<ClassImplements<?>, DynamicClass> nonSuperProxy = new HashMap<>();
  public static final Class<?>[] EMPTY_CLASSES = new Class[0];
  public static final Object[] EMPTY_ARGS = new Object[0];

  protected final DynamicMaker maker;

  protected ProxyMaker(DynamicMaker maker){
    this.maker = maker;
  }

  /**获取代理生成器的默认实现实例，需要提供一个{@linkplain DynamicMaker 动态生成器}以构建实例
   *
   * @param maker 创建动态对象使用的生成器
   * @param proxyHandler 用于代理处理的{@link ProxyMaker#invoke(DynamicObject, Caller, ArgumentList)}方法拦截，所有有效方法调用都会被拦截并传入此方法
   *
   * @return 默认实例*/
  public static ProxyMaker getDefault(DynamicMaker maker, ProxyHandler proxyHandler){
    return new ProxyMaker(maker){
      @Override
      public Object invoke(DynamicObject<?> proxy, Caller method, ArgumentList args){
        return proxyHandler.invoke(proxy, method, args);
      }
    };
  }

  public DynamicObject<Object> newProxyInstance(Class<?>[] interfaces){
    return newProxyInstance(Object.class, interfaces, EMPTY_ARGS);
  }

  public <T> DynamicObject<T> newProxyInstance(Class<T> base, Object... args){
    return newProxyInstance(base, EMPTY_CLASSES, null, args);
  }

  public <T> DynamicObject<T> newProxyInstance(Class<T> base, DynamicClass dynamicClass, Object... args){
    return newProxyInstance(base, EMPTY_CLASSES, dynamicClass, args);
  }

  public <T> DynamicObject<T> newProxyInstance(Class<T> base, Class<?>[] interfaces, Object... args){
    return newProxyInstance(base, interfaces, null, args);
  }

  /**创建一个代理实例，它的所有有效方法都已被提供的代理处理器拦截
   *
   * @param base 代理委托的java基类，代理实例可以正确的分配给此类型
   * @param interfaces 代理实现的接口列表，所有方法均会实现以调用代理处理函数
   * @param dynamicClass 代理实例的动态类型，可以为空
   * @param args 代理委托的类型中可用的构造函数的参数
   *
   * @return 一个代理实例*/
  public <T> DynamicObject<T> newProxyInstance(Class<T> base, Class<?>[] interfaces, DynamicClass dynamicClass, Object... args){
    DynamicClass dyClass = getProxyDyClass(dynamicClass, base, interfaces);

    DynamicObject<T> inst = maker.newInstance(base, interfaces, dyClass, args);

    if(dynamicClass != null){
      for(IFunctionEntry function: dynamicClass.getFunctions()){
        FunctionCaller caller = new FunctionCaller(function);
        inst.setFunc(function.getName(), (s, a) -> {
              try{
                return invoke(s, caller, a);
              }catch(Throwable e){
                throwException(e);
                return null;
              }
            },
            function.getType().getTypes());
      }
    }

    return inst;
  }

  /**从类和接口实现获取声明为代理的动态类型，参数给出的动态类型会作为该类型的直接超类，可以为空
   *
   * @param dynamicClass 结果动态类型的直接超类，为空时表示类型超类不明确
   * @param base 委托基类
   * @param interfaces 代理实现的接口
   *
   * @return 声明为代理实现的动态类型*/
  private <T> DynamicClass getProxyDyClass(DynamicClass dynamicClass, Class<T> base, Class<?>... interfaces){
    ClassImplements<T> impl = new ClassImplements<>(base, interfaces);
    DynamicClass dyc = dynamicClass == null? nonSuperProxy.get(impl): proxyMap.computeIfAbsent(dynamicClass, e -> new HashMap<>()).get(impl);

    if(dyc == null){
      dyc = dynamicClass == null? DynamicClass.get("defProxy$" + impl): DynamicClass.declare(dynamicClass + "$proxy$" + impl, dynamicClass);

      Class<?> dyBase = maker.getDynamicBase(base, interfaces);
      for(Method method: dyBase.getDeclaredMethods()){
        DynamicMaker.CallSuperMethod callSuper;
        if((callSuper = method.getAnnotation(DynamicMaker.CallSuperMethod.class)) != null){
          MethodCaller caller = new MethodCaller(method);
          dyc.setFunction(
              callSuper.srcMethod(),
              (s, a) -> {
                try{
                  return invoke(s, caller, a);
                }catch(Throwable e){
                  throwException(e);
                  return null;
                }
              },
              method.getParameterTypes()
          );
        }
      }

      (dynamicClass == null? nonSuperProxy: proxyMap.get(dynamicClass)).put(impl, dyc);
    }

    return dyc;
  }

  /**代理处理器，所有被代理的方法执行被拦截都会转入该方法，方法/函数都会以一个匿名函数的形式传递给这个方法
   * <p>默认实现调用会传入给出的匿名函数，否则子类应当按需要的代理处理方式实现此方法
   *
   * @param proxy 动态代理实例
   * @param method 被拦截的方法
   * @param args 实参列表
   *
   * @return 返回值*/
  protected abstract Object invoke(DynamicObject<?> proxy, Caller method, ArgumentList args) throws Throwable;

  /**异常处理器，当代理运行中发生任何异常都会转入此方法进行处理，默认直接封装为RuntimeException抛出
   *
   * @param thr 运行过程中捕获的异常*/
  public void throwException(Throwable thr){
    throw new RuntimeException(thr);
  }

  public class MethodCaller implements Caller{
    private static final Object[][] argLis = new Object[32][];

    static {
      for(int i = 0; i < argLis.length; i++){
        argLis[i] = new Object[i + 1];
      }
    }

    private final MethodHandle handler;
    private final String signature;

    private final Method method;
    private final FunctionType type;

    private MethodCaller(Method method){
      this.method = method;
      this.type = FunctionType.from(method);
      signature = method.getDeclaringClass().getName() + "." + FunctionType.signature(method);

      MethodHandles.Lookup lookup = MethodHandles.lookup();
      try{
        handler = lookup.unreflect(method);
      }catch(IllegalAccessException e){
        throw new RuntimeException(e);
      }
    }

    @Override
    public String getName(){
      return method.getName();
    }

    @Override
    public FunctionType getType(){
      return type;
    }

    public Object invoke(DynamicObject<?> proxy, ArgumentList args){
      Object[] arg = argLis[args.args().length];
      arg[0] = proxy;

      if(args.args().length != 0) System.arraycopy(args.args(), 0, arg, 1, args.args().length);

      try{
        return handler.invokeWithArguments(arg);
      }catch(Throwable e){
        throwException(e);
        return null;
      }
    }

    @Override
    public String toString(){
      return "method: " + signature;
    }
  }

  public static class FunctionCaller implements Caller{
    private final String signature;

    private final IFunctionEntry entry;

    private final Function<?, Object> function;

    private FunctionCaller(IFunctionEntry functionEntry){
      this.entry = functionEntry;
      this.function = functionEntry.getFunction();
      this.signature = functionEntry.getName() + functionEntry.getType();
    }

    @Override
    public String getName(){
      return entry.getName();
    }

    @Override
    public FunctionType getType(){
      return entry.getType();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object invoke(DynamicObject<?> self, ArgumentList args){
      return function.invoke((DynamicObject) self, args);
    }

    @Override
    public String toString(){
      return "function: " + signature;
    }
  }

  /**调用封装器，提供了一个方法{@link Caller#invoke(DynamicObject, ArgumentList)}方法来直接调用这个对象封装的方法或者函数*/
  public interface Caller{
    String getName();

    FunctionType getType();

    Object invoke(DynamicObject<?> self, ArgumentList args);

    default Object invoke(DynamicObject<?> self, Object... args){
      ArgumentList lis = ArgumentList.as(args);
      Object r = invoke(self, lis);
      lis.type().recycle();
      lis.recycle();
      return r;
    }

    default Object invoke(DynamicObject<?> self, FunctionType type, Object... args){
      ArgumentList lis = ArgumentList.asWithType(type, args);
      Object r = invoke(self, lis);
      lis.recycle();
      return r;
    }
  }

  public interface ProxyHandler{
    Object invoke(DynamicObject<?> proxy, Caller superFunction, ArgumentList args);
  }
}
