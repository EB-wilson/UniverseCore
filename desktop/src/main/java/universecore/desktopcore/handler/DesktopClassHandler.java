package universecore.desktopcore.handler;

import mindustry.Vars;
import universecore.desktopcore.classes.DesktopDynamicClassLoader;
import universecore.desktopcore.classes.DesktopGeneratedClassLoader;
import universecore.desktopcore.proxy.DesktopProxy;
import universecore.util.mods.ModInfo;
import universecore.util.classes.BaseDynamicClassLoader;
import universecore.util.handler.AbstractClassHandler;
import universecore.util.proxy.BaseProxy;
import universecore.util.proxy.BaseProxy.IllegalProxyHandlingException;
import universecore.util.proxy.IProxy;

import java.util.HashMap;

/**类处理器的平台实现类，在UncCore中保存了一个默认实例，或者单独实例化使用
 * @author EBwilson
 * @since 1.2*/
public class DesktopClassHandler extends AbstractClassHandler{
  private final static BaseDynamicClassLoader dynamicLoader = new DesktopDynamicClassLoader(Vars.mods.mainLoader());
  
  public DesktopClassHandler(ModInfo callerMod, ClassLoader modLoader){
    super(callerMod, new DesktopGeneratedClassLoader(callerMod, jarList.getCacheFile(callerMod).file(), Vars.mods.mainLoader()), modLoader);
  }

  /**用于获取目标类型的代理容器的工厂方法，代理子目标有一唯一限定名称，用于在所有此类型的代理容器中索引唯一目标
   * @param clazz 要代理的目标类型
   * @param name 代理容器的唯一限定名称
   * @return 代理容器*/
  @SuppressWarnings("unchecked")
  @Override
  public <T> BaseProxy<T> getProxy(Class<T> clazz, String name){
    return (BaseProxy<T>) proxies.computeIfAbsent(clazz, e -> new HashMap<>())
        .computeIfAbsent(name, e -> new DesktopProxy<>(clazz, getCurrentLoader(), getModClassLoader(), this));
  }

  /**扩展一个已经初始化的代理，当你需要为一个已初始化的代理添加代理方法，则你必须引用这个方法来获取代理的子代理
   * <p>注意，子代理与父类的代理处理器相对独立但仍保持向上调用，若你变更了父代理的行为，那么子代理若有向上引用，那么子代理的行为也会发生改变
   * <p>如果你要扩展的代理尚未初始化，那么请直接添加代理方法而不是用此方法扩展它，这会导致抛出IllegalProxyHandleException
   * @param proxy 要扩展的父代理
   * @param name 代理容器的唯一限定名称
   * @return 产生的子代理
   * @throws IllegalProxyHandlingException 如果代理的目标现在尚未初始化*/
  @Override
  public <T> BaseProxy<? extends T> extendProxy(IProxy<T> proxy, String name){
    if(! proxy.isInitialized()) throw new IllegalProxyHandlingException("can not extend an uninitialized proxy");
    BaseProxy<? extends T> result = getProxy(proxy.getProxyClass(), name);
    result.setSuperProxy(proxy);
    return result;
  }
  
  @Override
  public BaseDynamicClassLoader getDynamicLoader(){
    return dynamicLoader;
  }
}
