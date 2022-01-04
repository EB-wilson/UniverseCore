package universeCore.util.proxy;

import arc.Core;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**代理处理器的基类，保存了一系列方法的代理策略，代理类生成由于平台差异，安卓与桌面各自实现
 * 尽可能先完成所有代理方法的标记后再构建代理对象，以避免多次代理生成过多的代理类占用内存
 * 请注意，在构建一个代理实例后，代理类即刻被初始，已初始化的代理类不允许再编辑新的方法代理
 * 但你可以对已经被代理的方法进行变更，这对已生成的实例依然有效*/
@SuppressWarnings("unchecked")
public abstract class BaseProxy<Target>{
  protected static final String PROXY_METHOD = "$proxyHandle$";
  protected static final String LAMBDA_SUPER = "$lambdaSuper$";
  
  protected final HashMap<Method, ProxyMethod<?, Target>> proxies = new HashMap<>();
  protected final Class<Target> clazz;
  
  protected Class<? extends Target> proxyClass;
  
  protected final ArrayList<Constructor<? extends Target>> constructors = new ArrayList<>();
  protected final ArrayList<List<Class<?>>> cstrParamAssign = new ArrayList<>();
  
  private boolean initialized = false;
  
  protected BaseProxy(Class<Target> clazz){
    this.clazz = clazz;
  }
  
  protected abstract <T extends Target> Class<T> getProxyClass();
  
  /**
   * 对指定的目标方法添加一个代理方法，如果方法已存在，则将已存在的方法作为super传入代理操作
   * 注意，对已经代理的方法再添加代理会直接影响本代理产生的所有代理实例的行为，并且这是实时生效的
   * 如果你需要抽象另一组代理，那么请生成此代理对象的子代理
   *
   * @param method  将要代理的方法
   * @param handler 代理处理器，会传入对象本身，superHandler以及调用方法的参数数组，其中superHandler是调用链，不一定是被拦截的方法，可能是多层的调用关系
   * @throws IllegalProxyHandlingException 如果在类已经初始化后再对未注册的方法添加代理
   */
  public <R> void addMethodProxy(Method method, ProxyHandler<R, Target> handler){
    checkProxible(method);
    
    ProxyMethod<R, Target> proxy = (ProxyMethod<R, Target>) proxies.get(method);
    if(proxy != null){
      InvokeChains<R, Target> last = proxy.proxy;
      proxy.proxy = new InvokeChains<>();
      proxy.proxy.last = last;
      proxy.proxy.handle = handler;
    }
    else{
      if(initialized) throw new IllegalProxyHandlingException(this);
      
      proxy = new ProxyMethod<>(proxies.size(), method);
      proxy.proxy.handle = handler;
      proxies.put(method, proxy);
    }
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Target> T create(Target target, Object... param){
    if(!initialized){
      proxyClass = getProxyClass();
      initialized = true;
    }
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
      for(ProxyMethod<?, Target> proxy : proxies.values()){
        String key = PROXY_METHOD + proxy.id;
        setValueD(proxyClass.getDeclaredField(key), result, proxy);
      }
      ((IProxied)result).afterHandle();
      return result;
    }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e){
      throw new RuntimeException(e);
    }catch(NoSuchMethodException e){
      throw new IllegalProxyHandlingException(paramType);
    }
  }
  
  public void assignConstruct(Constructor<Target> cstr){
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
  
  private void checkProxible(Method method){
    int modifiers = method.getModifiers();
    if((modifiers & (Modifier.STATIC | Modifier.FINAL)) != 0 || (modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) throw new IllegalProxyHandlingException(method);
  }
  
  public static class ProxyMethod<Return, Self>{
    private final int id;
    private final Method targetMethod;
    private final InvokeChains<Return, Self> superMethod = new InvokeChains<>();
    
    private InvokeChains<Return, Self> proxy;
    
    private ProxyMethod(int id, Method targetMethod){
      this.id = id;
      this.targetMethod = targetMethod;
      this.proxy = new InvokeChains<>();
      proxy.last = superMethod;
    }
    
    public int id(){
      return id;
    }
    
    public Method targetMethod(){
      return targetMethod;
    }
    
    public InvokeChains<Return, Self> proxy(){
      return proxy;
    }
    
    public void superMethod(ProxyHandler<Return, Self> handle){
      superMethod.handle = handle;
    }
    
    public Return invoke(Self self, Object... args){
      return proxy.handle.invoke(self, proxy, args);
    }
  }
  
  public static class InvokeChains<Return, Self>{
    ProxyHandler<Return, Self> handle;
    InvokeChains<Return, Self> last;
    
    public Return callSuper(Self self, Object... args){
      return last.handle.invoke(self, last, args);
    }
  }
  
  public static class IllegalProxyHandlingException extends RuntimeException{
    public IllegalProxyHandlingException(BaseProxy<?> target){
      super("can not create a new proxy method in \"" + target + "\", this proxy was initialized");
    }
    
    public IllegalProxyHandlingException(Method method){
      super("can not proxy an method with modifiers \"" + Modifier.toString(method.getModifiers()) + "\" in method: \"" + method + "\"");
    }
    
    public IllegalProxyHandlingException(Constructor<?> cstr){
      super("before use a parametric constructor, you must be assign this constructor: " + cstr);
    }
    
    public IllegalProxyHandlingException(Class<?>[] params){
      super("no such constructor with parameter: " + Arrays.toString(params));
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
}
