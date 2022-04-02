package universecore.util.proxy;

/**代理处理使用的内部接口，非必要请不要调用
 * @author EBwilson
 * @since 1.2*/
public interface IProxied{
  void afterHandle();
  
  <T> BaseProxy<T> getProxyContainer();
}
