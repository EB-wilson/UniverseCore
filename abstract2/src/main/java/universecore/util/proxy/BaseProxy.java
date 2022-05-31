package universecore.util.proxy;

import universecore.util.handler.AbstractClassHandler;
import universecore.util.handler.ClassHandler;
import universecore.util.handler.FieldHandler;
import universecore.util.classes.AbstractFileClassLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

/**这个类型为代理容器的基类，提供了创建代理的所有方法，代理类生成的方法进入平台实现类。
 * 此方法不允许直接通过构造函数初始化，请使用{@link universecore.util.handler.AbstractClassHandler}中的工厂方法<strong>{@link universecore.util.handler.AbstractClassHandler#getProxy(Class, String)}</strong>。
 * <p>通过工厂方法指定一个被代理类型后，可获得一个代理容器，调用容器的
 * <strong>{@link BaseProxy#addMethodProxy(Method method, ProxyHandler handler)}</strong>方法向容器中添加代理方法，例如：
 * <pre>{@code
 * BaseProxy<Test> proxy = UncCore.classes.getProxy(Test.class);
 * Method method = Test.class.getMethod("run");
 *
 * proxy.addMethodProxy(method, (self, superHandle, args) -> {
 *   Object result = superHandle.callSuper(self, args);
 *   System.out.println("hello world");
 *   return result;
 * });
 *
 * Test t = proxy.create(null);
 * t.run();
 * }</pre>
 *
 * <pre>类处理器的引用从UncCore取默认操作器实例，上述例子会在Test的代理类中run()方法后添加操作，使运行run方法后打印一条信息“hello world”</pre>
 *
 * <p>其中Method即反射获取的对象方法，需要注意，代理的方法必须是public或者protected且不为final与static的成员方法，
 * 否则会抛出IllegalProxyHandleException。
 *
 * <p>无论何时，在一个代理容器初始化后，都不允许再对代理添加新的代理方法，否则会抛出IllegalProxyHandleException，
 * 但你仍然可以对已经创建代理的方法添加调用链，这将会改变容器的代理实例的行为，并且这是实时生效的。
 *
 * <p>另外，你可以对已被代理的容器调用{@link universecore.util.handler.AbstractClassHandler#extendProxy(IProxy, String)}来扩展代理类的代理容器，以便添加新的方法代理。
 * 此操作的行为与继承重写一致，此方法会创建一个新的代理容器，代理环境与被代理容器相对独立，更改此容器的调用链不会影响到被扩展的代理容器的调用链。
 *
 * <p>关于调用链，请参见
 * <p>{@link ProxyMethod.ProxyChains}
 *
 * @param <Target> 此代理应用到的类型
 * @author EBwilson
 * @since 1.2*/
@SuppressWarnings("unchecked")
public abstract class BaseProxy<Target> implements IProxy<Target>{
  private final ClassHandler handler;
  private static final String[] hexList = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
  
  protected static final String PROXY_METHOD = "$proxyHandle$";
  protected static final String LAMBDA_SUPER = "$lambdaSuper$";
  
  protected final HashMap<Method, ProxyMethod<?, Target>> proxies = new HashMap<>();
  protected final Class<Target> clazz;
  
  protected BaseProxy<? super Target> superProxy;
  public Class<? extends Target> proxyClass;
  
  protected final HashMap<MethodType, MethodHandle> constructors = new HashMap<>();
  protected final ArrayList<Constructor<Target>> assignedCstr = new ArrayList<>();
  protected final AbstractFileClassLoader genLoader;
  
  private boolean initialized = false;
  private boolean proxyPackagePrivate = false;
  
  protected BaseProxy(Class<Target> clazz, AbstractFileClassLoader loader, AbstractClassHandler handler){
    this.clazz = clazz;
    genLoader = loader;
    this.handler = handler;
    if(handler == null) throw new NullPointerException("ClassHandler can not be null");
  }
  
  protected abstract <T extends Target> Class<T> generateProxyClass();
  
  protected final <T extends Target> Class<T> loadClass(String name, Class<?> neighbor) throws ClassNotFoundException{
    return (Class<T>)(proxyPackagePrivate? genLoader.loadClass(name, neighbor) : genLoader.loadClass(name));
  }
  
  @Override
  public <T extends Target> Class<T> getProxyClass(){
    if(!initialized) initial();
    return (Class<T>)proxyClass;
  }
  
  @Override
  public void setSuperProxy(IProxy<? super Target> superProxy){
    if(clazz != superProxy.getProxyClass()) throw new IllegalProxyHandlingException(this, (BaseProxy<?>) superProxy);
    this.superProxy = (BaseProxy<? super Target>) superProxy;
  }
  
  @Override
  public boolean isInitialized(){
    return initialized;
  }
  
  protected String randomHexCode(){
    StringBuilder result = new StringBuilder();
    Random random = new Random();
    for(int i = 0; i < 32; i++){
      result.append(hexList[random.nextInt(hexList.length)]);
    }
    return result.toString();
  }
  
  @Override
  public <R> void addMethodProxy(Method method, ProxyHandler<R, Target> handler){
    int modifiers = method.getModifiers();
    checkProxible(modifiers);
    if((modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) proxyPackagePrivate = true;
    
    ProxyMethod<R, Target> proxy = (ProxyMethod<R, Target>) proxies.get(method);
    if(proxy != null){
      proxy.add(handler);
    }
    else{
      if(initialized) throw new IllegalProxyHandlingException(this);
      proxy = new ProxyMethod<>(proxies.size(), method);
      proxy.proxyHandle.handle = handler;
      proxies.put(method, proxy);
    }
  }
  
  @Override
  public <R> void removeMethodProxy(Method method, ProxyHandler<R, Target> handler){
    int modifiers = method.getModifiers();
    checkProxible(modifiers);
  
    ProxyMethod<R, Target> proxy = (ProxyMethod<R, Target>) proxies.get(method);
    if(proxy == null) throw new IllegalProxyHandlingException("can not remove a proxy method because this method was not proxied");
    proxy.remove(handler);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Target> T create(Target target, Object... param){
    if(!initialized) initial();
    Class<?>[] paramType = new Class[param.length];
    for(int i=0; i<param.length; i++){
      paramType[i] = asBasic(param[i].getClass());
    }
    try{
      MethodHandle cstr = null;

      tag: for(Map.Entry<MethodType, MethodHandle> entry: constructors.entrySet()){
        Class<?>[] methodArgs = entry.getKey().parameterArray();
        if(methodArgs.length != paramType.length) continue;

        for(int i = 0; i < methodArgs.length; i++){
          if(!methodArgs[i].isAssignableFrom(paramType[i]))continue tag;
        }

        cstr = entry.getValue();
        break;
      }

      if(cstr == null) throw new NoSuchMethodException();

      T result = (T) cstr.invokeWithArguments(param);
      if(target != null) cloneData(target, result);
      setProxyField(result);
      ((IProxied)result).afterHandle();
      return result;
    }catch(NoSuchMethodException e){
      throw new IllegalProxyHandlingException(paramType);
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }
  
  private <T extends Target> void setProxyField(T target){
    if(superProxy != null) superProxy.setProxyField(target);
    for(ProxyMethod<?, Target> proxy : proxies.values()){
      String key = PROXY_METHOD + proxy.id;
      FieldHandler.setValueDefault(target, key, proxy);
    }
  }

  private void setProxyData(){
    FieldHandler.setValueDefault(proxyClass, "proxyContainer", this);

    try{
      MethodHandles.Lookup lookup = MethodHandles.lookup();

      for(Constructor<Target> cstr: assignedCstr){
        proxyClass.getDeclaredConstructor(cstr.getParameterTypes());
        MethodHandle handle = lookup.unreflectConstructor(cstr);
        constructors.put(handle.type(), handle);
      }
    }catch(NoSuchMethodException|IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void initial(){
    if(superProxy != null && !superProxy.isInitialized()) superProxy.initial();
    if(initialized) throw new IllegalProxyHandlingException("can not initial a proxy after it was initialized");

    if(assignedCstr.isEmpty()){
      try{
        Constructor<Target> superDefCstr = clazz.getDeclaredConstructor();
        int mod = superDefCstr.getModifiers();
        if((mod & Modifier.PUBLIC) != 0 | (mod & Modifier.PROTECTED) != 0)
        assignConstruct(superDefCstr);
      }catch(NoSuchMethodException ignored){
      }
    }

    proxyClass = generateProxyClass();
    setProxyData();
    initialized = true;
  }
  
  @Override
  public void assignConstruct(Constructor<Target> cstr){
    checkProxible(cstr.getModifiers());
    if(initialized) throw new IllegalProxyHandlingException(this);
    
    assignedCstr.add(cstr);
  }
  
  /**将指定的对象的所有信息复制到目标对象中，目标对象必须与来源是同一个类的实例，或者是其子类的实例，注意，在克隆数据后，复制的目标对象保存的所有字段信息都将丢失
   * @param from 复制数据的来源
   * @param to 将要复制数据到的目标对象*/
  public static <F, T extends F> void cloneData(F from, T to){
    Class<?> deep = from.getClass();
    while(deep != Object.class){
      for(Field field : deep.getDeclaredFields()){
        if((field.getModifiers() & Modifier.STATIC) != 0) continue;
        String key = field.getName();
        FieldHandler.setValueDefault(to, key, FieldHandler.getValueDefault(from, key));
      }
      deep = deep.getSuperclass();
    }
  }
  
  protected static Class<?> asBasic(Class<?> clazz){
    if(clazz == Integer.class) return int.class;
    if(clazz == Float.class) return float.class;
    if(clazz == Double.class) return double.class;
    if(clazz == Long.class) return long.class;
    if(clazz == Boolean.class) return boolean.class;
    if(clazz == Short.class) return short.class;
    if(clazz == Byte.class) return byte.class;
    return clazz;
  }
  
  private void checkProxible(int modifiers){
    if(!handler.isGenerateFinished()){
      if((modifiers & (Modifier.STATIC | Modifier.FINAL | Modifier.PRIVATE)) != 0)
        throw new IllegalProxyHandlingException(modifiers);
    }
    else if((modifiers & (Modifier.STATIC | Modifier.FINAL)) != 0 || (modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0){
      if((modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0)
        throw new IllegalProxyHandlingException("when classes generate finished, dynamic generate class can not create a proxy on a package private method");
      throw new IllegalProxyHandlingException(modifiers);
    }
  }
  
  /**代理方法的容器，程序处理时引用，包含了对调用链的变更和控制等的方法封装，但通常不会主动用到这个类*/
  public static class ProxyMethod<Return, Self> implements Iterable<ProxyMethod<Return, Self>.ProxyChains>{
    private final int id;
    private final Method targetMethod;
    private final ProxyChains superMethod = new ProxyChains();
  
    private ProxyChains proxyHandle;
    private final ChainsIterator iterator = new ChainsIterator();
  
    private ProxyMethod(int id, Method targetMethod){
      this.id = id;
      this.targetMethod = targetMethod;
      this.proxyHandle = new ProxyChains();
      proxyHandle.previous = superMethod;
    }
  
    public int id(){
      return id;
    }
  
    public Method targetMethod(){
      return targetMethod;
    }
  
    public ProxyChains proxy(){
      return proxyHandle;
    }
    
    public void add(ProxyHandler<Return, Self> added){
      ProxyChains last = proxyHandle;
      proxyHandle = new ProxyChains();
      proxyHandle.previous = last;
      proxyHandle.handle = added;
    }
    
    public void remove(){
      proxyHandle = proxyHandle.previous;
    }
    
    public void remove(ProxyHandler<Return, Self> removed){
      ProxyChains curr = proxyHandle, last;
      while(curr.previous != null){
        last = curr;
        curr = curr.previous;
        if(curr.handle.equals(removed)){
          last.previous = curr.previous;
          break;
        }
      }
    }
    
    public void remove(Function<ProxyHandler<Return, Self>, Boolean> removed){
      ProxyChains curr = proxyHandle, last;
      while(curr.previous != null){
        last = curr;
        curr = curr.previous;
        if(removed.apply(curr.handle)){
          last.previous = curr.previous;
          break;
        }
      }
    }
    
    public void remove(int deep){
      int cd = 0;
      ProxyChains curr = proxyHandle, last;
      while(curr.previous != null){
        last = curr;
        curr = curr.previous;
        cd++;
        if(deep == cd){
          last.previous = curr.previous;
          break;
        }
      }
    }
    
    public void superMethod(ProxyHandler<Return, Self> handle){
      superMethod.handle = handle;
    }
  
    public Return invoke(Self self, Object... args){
      return proxyHandle.handle.invoke(self, proxyHandle, args);
    }
  
    @Override
    public Iterator<ProxyChains> iterator(){
      iterator.current = proxyHandle;
      return iterator;
    }
    
    private class ChainsIterator implements Iterator<ProxyChains>{
      ProxyChains current;
      
      @Override
      public boolean hasNext(){
        return current.previous != null;
      }
  
      @Override
      public ProxyChains next(){
        ProxyChains result = current.previous;
        current = current.previous;
        return result;
      }
    }
  
    /**调用链的容器，保存了此次调用以及callSuper将要调用的上一个InvokeChains，这是一个基于链表实现的容器，对方法添加的代理行为实质上就是创建一个InvokeChains，并将其超级方法的访问放置到前一个链上
     * <p>对于代理类的最原始的InvokeChains会由程序处理使之指向被代理类的此方法（对于代理类而言这其实就是super方法），而代理实例只会调用最后被添加的InvokeSuper，并将这个InvokeSuper的前一个链作为参数传递给ProxyHandler
     *
     * <pre>{@code
     * 例如有一个类
     * public class Sample{
     *   public void test(){
     *     System.out.println("origin");
     *   }
     * }
     *
     * 为这个类的test方法创建了代理，并先后添加了两个行为：
     * lambda1:
     * (self, superHandle, args) -> {
     *   System.out.println("first proxy");
     *   superHandle.callSuper(self. args);
     *   return null;
     * }
     *
     * lambda2:
     * (self, superHandle, args) -> {
     *   superHandle.callSuper(self. args);
     *   System.out.println("second proxy");
     *   return null;
     * }
     *
     * 那么当调用test时，这个模型的工作过程大致可以表示为：
     *  Sample-proxy── ── ── ── ── ── ──►test()──►println("origin")
     *       │          1 callSuper        ▲   4 call
     *       └──►lambda2──────►lambda1─────┘
     *             │ 5 call       │        3 callSuper
     *             ▼              │ 2 call
     * println("second proxy")    │
     *                            ▼
     *                 println("first proxy")
     * }</pre>
     *
     * 方法在调用时的向上引用即是由作为lambda第二个参数传入的调用链作为方法的父级承担，而如果你不引用callSuper，向上引用会很自然的中断掉。
     * <p>调用链中的每一个环节都可以被即时的更改，整个链本身也是可变以及可迭代的，可通过{@link BaseProxy}内提供的方法去改变链的结构与行为，这些变更都会即时生效。
     * <p>因此你可以在编辑代理时像钩子一样将一个个代理方法添加到目标方法的调用链上。
     * <p>关于封装的链直接控制行为，请参见：{@link ProxyMethod}
     * */
    public class ProxyChains implements InvokeChains<Return, Self>{
      ProxyHandler<Return, Self> handle;
      ProxyChains previous;
    
      /**引用前一个调用链保存的方法，可以在调用链中使用，此方法会调用前一个调用链保存的ProxyHandler，并将这两个参数以及前一条访问链组成参数表传入
       * @param self 引用自身的指针
       * @param args 传入参数列表
       * @return 上一个调用链handle的返回结果*/
      public Return callSuper(Self self, Object... args){
        return previous.handle.invoke(self, previous, args);
      }
      
      public Method superMethod(){
        return targetMethod;
      }
    }
  }

  public static class IllegalProxyHandlingException extends RuntimeException{
    public IllegalProxyHandlingException(BaseProxy<?> target){
      super("can not create a new proxy method or constructor in \"" + target + "\", this proxy was initialized");
    }
    
    public IllegalProxyHandlingException(int modifiers){
      super("can not proxy an method or constructor with modifiers \"" + Modifier.toString(modifiers));
    }
    
    public IllegalProxyHandlingException(Class<?>[] params){
      super("no such constructor with parameter: " + Arrays.toString(params));
    }
  
    public IllegalProxyHandlingException(String info){
      super(info);
    }
  
    public IllegalProxyHandlingException(BaseProxy<?> proxy, BaseProxy<?> superProxy){
      super("can not set super proxy as " + superProxy + ", it not assignable of " + proxy);
    }
  }
}
