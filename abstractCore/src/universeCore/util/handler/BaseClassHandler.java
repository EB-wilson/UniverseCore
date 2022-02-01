package universeCore.util.handler;

import arc.struct.ObjectMap;
import universeCore.util.classes.AbstractFileClassLoader;
import universeCore.util.classes.BaseDynamicClassLoader;
import universeCore.util.classes.BaseGeneratedClassLoader;
import universeCore.util.classes.JarList;
import universeCore.util.proxy.BaseProxy;
import universeCore.util.proxy.BaseProxy.IllegalProxyHandlingException;
import universeCore.util.proxy.ProxyHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

/**与类操作相关的一系列方法，根据平台区分实现，在UncCore类中会初始化一个默认实例
 * <p>但这个类的实现类ClassHandler并非平台敏感的，你可以直接调用和初始化平台实现类或者使用默认实例
 * <p><strong>注意，这个类并不包含所有方法描述，请使用实现类的类型传递实例</strong>
 * @author EBwilson
 * @since 1.2*/
public abstract class BaseClassHandler{
  /**默认调用父类方法的代理处理器实例*/
  public static final ProxyHandler<?, ?> DEFAULT_CALL_SUPER = (s, sup, arg) -> sup.callSuper(s, arg);
  
  protected final static JarList jarList = new JarList();
  protected final static ObjectMap<Class<?>, ObjectMap<String, BaseProxy<?>>> proxies = new ObjectMap<>();
  
  protected final JarList.ModInfo mod;
  private final ClassLoader modClassLoader;
  private final BaseGeneratedClassLoader generatedLoader;
  
  private boolean generateComplete = false;
  
  protected BaseClassHandler(JarList.ModInfo callerMod, BaseGeneratedClassLoader generatedLoader, ClassLoader modClassLoader){
    mod = callerMod;
    this.generatedLoader = generatedLoader;
    this.modClassLoader = modClassLoader;
  }
  
  /**结束此处理器的缓存文件生成，之后类型操作转换到动态控制，产生的类将不再进行缓存（或者说不在下次启动保留）
   * </p>注意，这个方法的调用时机必须是确定的，固定在每次启动都需要复用的类型操作全部完成之后调用，<strong>绝对不要在到达不可确定的运行时后才调用此方法</strong>*/
  public void generateFinish(){
    generateComplete = true;
  }
  
  public boolean isGenerateFinished(){
    return generateComplete;
  }
  
  /**用于获取目标类型的代理容器的工厂方法，代理子目标有一唯一限定名称，用于在所有此类型的代理容器中索引唯一目标
   * @param clazz 创建代理的目标类型
   * @param name 代理容器的唯一限定名称
   * @return 代理容器*/
  public abstract <T> BaseProxy<T> getProxy(Class<T> clazz, String name);
  
  /**获得指定类型的面向切面的全方法代理容器，注意，切面容器仅是对所有方法的一级代理调用链应用传入的代理处理器，你仍然可以按普通代理方法对此代理添加方法调用链
   * </p>此方法会对所有方法创建代理调用链，这意味着你可以将此方法用作创建全方法动态调用链，修改调用链同样适用，对调用链的修改仍然实时生效
   * @param clazz 创建代理的目标类型
   * @param proxyHandle 应用切面的代理处理器
   * @return AOP代理容器*/
  @SuppressWarnings("unchecked")
  public <T> BaseProxy<T> getAOPProxy(Class<T> clazz, ProxyHandler<?, T> proxyHandle){
    BaseProxy<T> proxy = null;
    boolean exist = false;
    ObjectMap<String, BaseProxy<?>> p = proxies.get(clazz);
    if(p != null){
      exist = (proxy = (BaseProxy<T>) p.get("AOP")) != null;
    }
    if(exist) return proxy;
    
    proxy = getProxy(clazz, "AOP");
    ArrayList<Method> mts = new ArrayList<>();
    Class<?> c = clazz;
    while(c != Object.class){
      mts.addAll(Arrays.asList(c.getDeclaredMethods()));
      c = c.getSuperclass();
    }
    int modifiers;
    for(Method method : mts){
      modifiers = method.getModifiers();
      if((modifiers & (Modifier.FINAL | Modifier.STATIC)) == 0){
        if(generateComplete){
          if((modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) proxy.addMethodProxy(method, proxyHandle);
        }
        else if((modifiers & (Modifier.PRIVATE)) == 0) proxy.addMethodProxy(method, proxyHandle);
      }
    }
    return proxy;
  }
  
  /**扩展一个已经初始化的代理，当你需要为一个已初始化的代理添加代理方法，则你必须引用这个方法来获取代理的子代理
   * <p>注意，子代理与父类的代理处理器相对独立但仍保持向上调用，若你变更了父代理的行为，那么子代理若有向上引用，那么子代理的行为也会发生改变
   * <p>如果你要扩展的代理尚未初始化，那么请直接添加代理方法而不是用此方法扩展它，这会导致抛出IllegalProxyHandleException
   * @param proxy 要扩展的父代理
   * @param name 代理容器的唯一限定名称
   * @return 产生的子代理
   * @throws IllegalProxyHandlingException 如果代理的目标现在尚未初始化*/
  public <T> BaseProxy<? extends T> extendProxy(BaseProxy<T> proxy, String name){
    if(! proxy.isInitialized()) throw new IllegalProxyHandlingException("can not extend an uninitialized proxy");
    BaseProxy<? extends T> result = getProxy(proxy.getProxyClass(), name);
    result.setSuperProxy(proxy);
    return result;
  }
  
  public AbstractFileClassLoader getCurrentLoader(){
    return generateComplete? getDynamicLoader(): generatedLoader;
  }
  
  public abstract BaseDynamicClassLoader getDynamicLoader();
  
  public ClassLoader getModClassLoader(){
    return modClassLoader;
  }
}
