package universecore.util.handler;

import universecore.util.mods.ModInfo;
import universecore.util.classes.AbstractFileClassLoader;
import universecore.util.classes.AbstractGeneratedClassLoader;
import universecore.util.classes.JarList;
import universecore.util.proxy.IProxy;
import universecore.util.proxy.ProxyHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**与类操作相关的一系列方法，根据平台区分实现
 * <p>但这个类的实现类ClassHandler并非平台敏感的，你可以直接调用和初始化平台实现类或者使用默认实例
 * <p><strong>注意，这个类并不包含所有方法描述，请使用实现类的类型传递实例</strong>
 * @author EBwilson
 * @since 1.2*/
public abstract class AbstractClassHandler implements ClassHandler{
  protected final static JarList jarList = new JarList();
  protected final static HashMap<Class<?>, HashMap<String, IProxy<?>>> proxies = new HashMap<>();

  protected final ModInfo mod;
  protected final ClassLoader loader;

  private final AbstractGeneratedClassLoader generatedLoader;
  private boolean generateComplete = false;
  
  protected AbstractClassHandler(ModInfo mod, AbstractGeneratedClassLoader generatedLoader, ClassLoader loader){
    this.mod = mod;
    this.generatedLoader = generatedLoader;
    this.loader = loader;
  }
  
  @Override
  public void generateFinish(){
    generateComplete = true;
  }
  
  @Override
  public boolean isGenerateFinished(){
    return generateComplete;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> IProxy<T> getAOPProxy(Class<T> clazz, ProxyHandler<?, T> proxyHandle){
    IProxy<T> proxy = null;
    boolean exist = false;
    HashMap<String, IProxy<?>> p = proxies.get(clazz);
    if(p != null){
      exist = (proxy = (IProxy<T>) p.get("AOP")) != null;
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
  
  @Override
  public <T> IProxy<? extends T> extendProxy(IProxy<T> proxy, String name){
    IProxy<? extends T> result = getProxy(proxy.getProxyClass(), name);
    result.setSuperProxy(proxy);
    return result;
  }
  
  @Override
  public AbstractFileClassLoader getCurrentLoader(){
    return generateComplete? getDynamicLoader(): generatedLoader;
  }

  protected ClassLoader getModClassLoader(){
    return loader;
  }
}
