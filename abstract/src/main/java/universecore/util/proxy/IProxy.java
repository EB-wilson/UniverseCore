package universecore.util.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public interface IProxy<Target>{
  /**
   * 获得此代理的代理类，不要使用这个类型反射获取实例，除非你知道如何给代理实例分配代理容器
   * <p><strong>此操作会初始化代理</strong>
   */
  <T extends Target> Class<T> getProxyClass();

  void setSuperProxy(IProxy<? super Target> superProxy);

  boolean isInitialized();

  /**
   * 对指定的目标方法添加一个代理方法，如果方法已存在，则将已存在的方法作为super传入代理操作。
   * 注意，对已经代理的方法再添加代理会直接影响本代理产生的所有代理实例的行为，并且这是实时生效的。
   *
   * @param method  将要代理的方法
   * @param handler 代理处理器，会传入对象本身，superHandler以及调用方法的参数数组，其中superHandler是调用链，不一定是被拦截的方法，可能是多层的调用关系
   */
  <R> void addMethodProxy(Method method, ProxyHandler<R, Target> handler);

  <R> void removeMethodProxy(Method method, ProxyHandler<R, Target> handler);

  /**
   * 用指定的参数创建一个代理实例，如果被代理对象不是null，那么会将被代理对象的所有信息拷贝到代理实例，但这可能会带来较大的性能开销，不建议频繁拷贝对象信息
   * <p>如果你调用的是含有参数的构造器，那么你必须先注册这个构造器
   * <p><strong>若此代理尚未初始化，此操作会初始化这个代理对象</strong>
   *
   * @param target 被代理的对象
   * @param param  构造函数参数，若不为空则必须先注册这个构造器
   * @return 生成的代理实例，该实例的类型继承自指定的被代理类
   */
  <T extends Target> T create(Target target, Object... param);

  /**
   * 初始化这个代理，通常不需要手动调用，或者说最好不要手动调用，否则这个方法可能会变成游戏的出口
   */
  void initial();

  /**
   * 注册一个构造器用于创建代理实例。要使用一个含有参数的构造器，你必须先将它注册到代理当中，代理初始化以后将不再允许添加构造器
   *
   * @param cstr 要进行注册的构造器
   */
  void assignConstruct(Constructor<Target> cstr);
}
