package universecore.util.handler;

import universecore.util.classes.AbstractDynamicClassLoader;
import universecore.util.classes.AbstractFileClassLoader;
import universecore.util.proxy.IProxy;
import universecore.util.proxy.ProxyHandler;

public interface ClassHandler{
  /**
   * 结束此处理器的缓存文件生成，之后类型操作转换到动态控制，产生的类将不再进行缓存（或者说不在下次启动保留）
   * </p>注意，这个方法的调用时机必须是确定的，固定在每次启动都需要复用的类型操作全部完成之后调用，<strong>绝对不要在到达不可确定的运行时后才调用此方法</strong>
   */
  void generateFinish();

  boolean isGenerateFinished();

  /**
   * 用于获取目标类型的代理容器的工厂方法，代理子目标有一唯一限定名称，用于在所有此类型的代理容器中索引唯一目标
   *
   * @param clazz 创建代理的目标类型
   * @param name  代理容器的唯一限定名称
   * @return 代理容器
   */
  <T> IProxy<T> getProxy(Class<T> clazz, String name);

  /**
   * 获得指定类型的面向切面的全方法代理容器，注意，切面容器仅是对所有方法的一级代理调用链应用传入的代理处理器，你仍然可以按普通代理方法对此代理添加方法调用链
   * </p>此方法会对所有方法创建代理调用链，这意味着你可以将此方法用作创建全方法动态调用链，修改调用链同样适用，对调用链的修改仍然实时生效
   *
   * @param clazz       创建代理的目标类型
   * @param proxyHandle 应用切面的代理处理器
   * @return AOP代理容器
   */
  <T> IProxy<T> getAOPProxy(Class<T> clazz, ProxyHandler<?, T> proxyHandle);

  /**
   * 扩展一个已经初始化的代理，当你需要为一个已初始化的代理添加代理方法，则你必须引用这个方法来获取代理的子代理
   * <p>注意，子代理与父类的代理处理器相对独立但仍保持向上调用，若你变更了父代理的行为，那么子代理若有向上引用，那么子代理的行为也会发生改变
   * <p>如果你要扩展的代理尚未初始化，那么请直接添加代理方法而不是用此方法扩展它
   *
   * @param proxy 要扩展的父代理
   * @param name  代理容器的唯一限定名称
   * @return 产生的子代理
   */
  <T> IProxy<? extends T> extendProxy(IProxy<T> proxy, String name);

  AbstractFileClassLoader getCurrentLoader();

  AbstractDynamicClassLoader getDynamicLoader();
}
