package universecore.util.proxy;

/**代理处理器的接口，这个接口有一个抽象方法invoke(Type self, InvokeChains superHandler, Object[] args);
 * 这个方法即调用时的访问入口，参数self会传入代理对象本身，superHandler是调用链，携带被代理方法的super方法，args为方法的参数列表，调用superHandler.callSuper会向上引用超级方法
 * 需要注意的是{@link universecore.util.proxy.BaseProxy.ProxyMethod.InvokeChains}是一条调用链，你可以多次对同一个方法执行addMethodProxy操作，而每一次添加，传入的superHandle都是上一次添加的调用链，并不是仅指向super方法的指针
 * @see universecore.util.proxy.BaseProxy.ProxyMethod.InvokeChains
 * @author EBwilson
 * @since 1.2*/
public interface ProxyHandler<R, S>{
  /**代理方法的调用链处理方法，代理类的代理方法或者调用链的向上引用都会执行时会调用这个方法
   * @param self 代理对象自身的指针
   * @param superHandle 调用处理，来自上一级的调用链，如果这是最后一级调用链，那么这会指向被代理方法的超级方法
   * @param param 由被代理方的参数组成的数组*/
  R invoke(S self, BaseProxy.ProxyMethod<R, S>.InvokeChains superHandle, Object[] param);
}
