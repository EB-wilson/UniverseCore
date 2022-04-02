package universecore.util.proxy;

import arc.Core;
import arc.func.Boolf;
import arc.util.Nullable;
import universecore.util.classes.AbstractFileClassLoader;
import universecore.util.handler.BaseClassHandler;

import java.lang.reflect.*;
import java.util.*;

/**这个类型为代理容器的基类，提供了创建代理的所有方法，代理类生成的方法进入平台实现类。
 * 此方法不允许直接通过构造函数初始化，请使用ClassHandler中的工厂方法<strong>{@code <T> BaseProxy<T> getProxy(Class<T> clazz)}</strong>。
 * <p>通过工厂方法指定一个被代理类型后，可获得一个代理容器，调用容器的
 * <strong>{@code addMethodProxy(Method method, ProxyHandler handler)}</strong>方法向容器中添加代理方法，例如：
 * <pre>{@code
 * BaseProxy<Test> proxy = UncCore.classes.getProxy(Test.class);
 * Method method = Test.class.getMethod("run");
 * proxy.addMethodProxy(method, (self, superHandle, args) -> {
 *   Object result = superHandle.callSuper(self, args);
 *   Log.info("hello world");
 *   return result;
 * });
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
 * <p>另外，你可以对已被代理的容器调用ClassHandler中的工厂方法<strong>{@code <T> BaseProxy<? extends T> extendProxy(BaseProxy<T> super)}</strong>
 * 来扩展代理类的代理容器，以便添加新的方法代理，其表现与继承重写一致，此方法会创建一个新的代理容器，代理环境与被代理容器相对独立，更改此容器的调用链不会影响到被代理容器的调用链。
 * @author EBwilson
 * @since 1.2*/
@SuppressWarnings("unchecked")
public abstract class BaseProxy<Target>{
  private final BaseClassHandler handler;
  private static final String[] hexList = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
  
  protected static final String PROXY_METHOD = "$proxyHandle$";
  protected static final String LAMBDA_SUPER = "$lambdaSuper$";
  
  protected final HashMap<Method, ProxyMethod<?, Target>> proxies = new HashMap<>();
  protected final Class<Target> clazz;
  
  @Nullable protected BaseProxy<? super Target> superProxy;
  protected Class<? extends Target> proxyClass;
  
  protected final ArrayList<Constructor<? extends Target>> constructors = new ArrayList<>();
  protected final ArrayList<List<Class<?>>> cstrParamAssign = new ArrayList<>();
  protected final AbstractFileClassLoader genLoader;
  
  private boolean initialized = false;
  private boolean proxyPackagePrivate = false;
  
  protected BaseProxy(Class<Target> clazz, AbstractFileClassLoader loader, BaseClassHandler handler){
    this.clazz = clazz;
    genLoader = loader;
    this.handler = handler;
    if(handler == null) throw new NullPointerException("ClassHandler can not be null");
  }
  
  protected abstract <T extends Target> Class<T> generateProxyClass();
  
  protected final <T extends Target> Class<T> loadClass(String name, Class<?> neighbor) throws ClassNotFoundException{
    return (Class<T>)(proxyPackagePrivate? genLoader.loadClass(name, neighbor) : genLoader.loadClass(name));
  }
  
  /**获得此代理的代理类，不要使用这个类型反射获取实例，除非你知道如何给代理实例分配代理容器
   * <p><strong>此操作会初始化代理</strong>*/
  public <T extends Target> Class<T> getProxyClass(){
    if(!initialized) initial();
    return (Class<T>)proxyClass;
  }
  
  public void setSuperProxy(BaseProxy<? super Target> superProxy){
    if(clazz != superProxy.getProxyClass()) throw new IllegalProxyHandlingException(this, superProxy);
    this.superProxy = superProxy;
  }
  
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
  
  /**对指定的目标方法添加一个代理方法，如果方法已存在，则将已存在的方法作为super传入代理操作
   * 注意，对已经代理的方法再添加代理会直接影响本代理产生的所有代理实例的行为，并且这是实时生效的
   * 如果你需要抽象另一组代理，那么请生成此代理对象的子代理
   *
   * @param method 将要代理的方法
   * @param handler 代理处理器，会传入对象本身，superHandler以及调用方法的参数数组，其中superHandler是调用链，不一定是被拦截的方法，可能是多层的调用关系
   * @throws IllegalProxyHandlingException 如果在类已经初始化后再对未注册的方法添加代理*/
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
      proxy.proxy.handle = handler;
      proxies.put(method, proxy);
    }
  }
  
  public <R> void removeMethodProxy(Method method, ProxyHandler<R, Target> handler){
    int modifiers = method.getModifiers();
    checkProxible(modifiers);
    if((modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) proxyPackagePrivate = true;
  
    ProxyMethod<R, Target> proxy = (ProxyMethod<R, Target>) proxies.get(method);
    if(proxy == null) throw new IllegalProxyHandlingException("can not remove a proxy method because this method was not proxied");
    proxy.remove(handler);
  }
  
  /**用指定的参数创建一个代理实例，如果被代理对象不是null，那么会将被代理对象的所有信息拷贝到代理实例，但这可能会带来大量的性能开销，如果不是很必要的话不建议这么做
   * 如果你调用的是含有参数的构造器，那么你必须先注册这个构造器
   * <p><strong>此操作会初始化这个代理</strong>
   * @param target 被代理的对象
   * @param param 构造函数参数，若不为空则必须先注册这个构造器
   * @return 生成的代理实例，该实例的类型继承自指定的被代理类
   * @throws IllegalProxyHandlingException 如果构造器不存在或者没有注册*/
  @SuppressWarnings("unchecked")
  public <T extends Target> T create(Target target, Object... param){
    if(!initialized) initial();
    Class<?>[] paramType = new Class[param.length];
    for(int i=0; i<param.length; i++){
      paramType[i] = asBasic(param[i].getClass());
    }
    try{
      Constructor<T> cstr = (Constructor<T>) proxyClass.getDeclaredConstructor(paramType);
      if(cstr.getParameterCount() > 0 && !cstrParamAssign.contains(Arrays.asList(cstr.getParameterTypes()))) throw new IllegalProxyHandlingException(cstr);
      cstr.setAccessible(true);
      T result = cstr.newInstance(param);
      if(target != null) cloneData(target, result);
      setProxyField(result);
      ((IProxied)result).afterHandle();
      return result;
    }catch(InstantiationException | IllegalAccessException | InvocationTargetException e){
      throw new RuntimeException(e);
    }catch(NoSuchMethodException e){
      throw new IllegalProxyHandlingException(paramType);
    }
  }
  
  private <T extends Target> void setProxyField(T target){
    if(superProxy != null) superProxy.setProxyField(target);
    try{
      for(ProxyMethod<?, Target> proxy : proxies.values()){
        String key = PROXY_METHOD + proxy.id;
        if(Core.app.isAndroid()){
          setValueA(proxyClass.getDeclaredField(key), target, proxy);
        }
        else setValueD(proxyClass.getDeclaredField(key), target, proxy);
      }
    }catch(NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }
  
  private void setProxyContainerField(){
    if(superProxy != null) superProxy.setProxyContainerField();
    try{
      Field containerField = proxyClass.getDeclaredField("proxyContainer");
      if(Core.app.isAndroid()){
        setValueA(containerField, null, this);
      }
      else setValueD(containerField, null, this);
    }catch(NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }
  
  /**初始化这个代理，通常不需要手动调用，或者说最好不要手动调用，不然这个方法可能会变成游戏的出口
   * @throws IllegalProxyHandlingException 如果这个代理已经被初始化过*/
  public void initial(){
    if(initialized) throw new IllegalProxyHandlingException("can not initial a proxy after it was initialized");
    proxyClass = generateProxyClass();
    setProxyContainerField();
    initialized = true;
  }
  
  /**注册一个构造器用于创建代理实例，要调用一个含有参数的构造器，你必须先行注册，代理初始化以后，同样不再允许添加构造器
   * @param cstr 要进行注册的构造器
   * @throws IllegalProxyHandlingException 如果代理已经初始化了*/
  public void assignConstruct(Constructor<Target> cstr){
    checkProxible(cstr.getModifiers());
    if(initialized) throw new IllegalProxyHandlingException(this);
    
    constructors.add(cstr);
    cstrParamAssign.add(Arrays.asList(cstr.getParameterTypes()));
  }
  
  /**将指定的对象的所有信息复制到目标对象中，目标对象必须与来源是同一个类的实例，或者是其子类的实例
   * @param from 复制数据的来源
   * @param to 将要复制数据到的目标对象*/
  public static <F, T extends F> void cloneData(F from, T to){
    Class<?> deep = from.getClass();
    while(deep != Object.class){
      for(Field field : deep.getDeclaredFields()){
        if(Core.app.isAndroid()){
          setValueA(field, to, getValue(field, from));
        }
        else setValueD(field, from, getValue(field, from));
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
  
  private static void setValueD(Field field, Object obj, Object value){
    try{
      int modifiers = field.getModifiers();
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.set(field, modifiers & ~ Modifier.FINAL);
      field.setAccessible(true);
      field.set(obj, value);
    }catch(NoSuchFieldException | IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }
  
  private static void setValueA(Field field, Object obj, Object value){
    try{
      field.setAccessible(true);
      field.set(obj, value);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }
  
  private static Object getValue(Field field, Object target){
    field.setAccessible(true);
    try{
      return field.get(target);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }
  
  /**代理方法的容器，程序处理时引用，通常不会主动用到这个类*/
  public static class ProxyMethod<Return, Self> implements Iterable<ProxyMethod<Return, Self>.InvokeChains>{
    private final int id;
    private final Method targetMethod;
    private final InvokeChains superMethod = new InvokeChains();
  
    private InvokeChains proxy;
    private final ChainsIterator iterator = new ChainsIterator();
  
    private ProxyMethod(int id, Method targetMethod){
      this.id = id;
      this.targetMethod = targetMethod;
      this.proxy = new InvokeChains();
      proxy.previous = superMethod;
      iterator.current = superMethod;
    }
  
    public int id(){
      return id;
    }
  
    public Method targetMethod(){
      return targetMethod;
    }
  
    public InvokeChains proxy(){
      return proxy;
    }
    
    public void add(ProxyHandler<Return, Self> added){
      InvokeChains last = proxy;
      proxy = new InvokeChains();
      proxy.previous = last;
      proxy.handle = added;
      iterator.current = proxy;
    }
    
    public void remove(){
      proxy = proxy.previous;
    }
    
    public void remove(ProxyHandler<Return, Self> removed){
      InvokeChains curr = proxy, last;
      while(curr.previous != null){
        last = curr;
        curr = curr.previous;
        if(curr.handle.equals(removed)){
          last.previous = curr.previous;
          break;
        }
      }
    }
    
    public void remove(Boolf<ProxyHandler<Return, Self>> removed){
      InvokeChains curr = proxy, last;
      while(curr.previous != null){
        last = curr;
        curr = curr.previous;
        if(removed.get(curr.handle)){
          last.previous = curr.previous;
          break;
        }
      }
    }
    
    public void remove(int deep){
      int cd = 0;
      InvokeChains curr = proxy, last;
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
      return proxy.handle.invoke(self, proxy, args);
    }
  
    @Override
    public Iterator<InvokeChains> iterator(){
      return iterator;
    }
    
    private class ChainsIterator implements Iterator<InvokeChains>{
      InvokeChains current;
      
      @Override
      public boolean hasNext(){
        return current.previous != null;
      }
  
      @Override
      public InvokeChains next(){
        return current.previous;
      }
    }
  
    /**调用链的容器，保存了此次调用以及callSuper将要调用的上一个InvokeChains，这是一个类似链表的容器，你对方法添加的代理操作实质上就是创建一个InvokeChains，并将super访问放置到前一个
     * 对于代理类的最原始的InvokeChains会由程序处理使之指向被代理类的此方法（对于代理类而言这其实就是super方法），而代理实例永远只会调用最后被添加的InvokeSuper，并将这个InvokeSuper的前一个链作为参数传递给ProxyHandler
     * 因此你可以在编辑代理时像钩子一样将一个个代理方法添加到目标方法的调用链上*/
    public class InvokeChains{
      ProxyHandler<Return, Self> handle;
      InvokeChains previous;
    
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
    
    public IllegalProxyHandlingException(Constructor<?> cstr){
      super("before use a parametric constructor, you must be assign this constructor: " + cstr);
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
