package universeCore.util.proxy;

public interface ProxyHandler<R, S>{
  R invoke(S self, BaseProxy.InvokeChains<R, S> superHandle, Object[] param);
}
