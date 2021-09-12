package universeCore.util.handler;

import arc.func.Cons;
import arc.func.Prov;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.util.Log;

@SuppressWarnings("unchecked")
public class ProxyHandler{
  public static <T> T getProxyInstance(Class<T> type, MethodInterceptor inter){
    return (T) null;
  }
  
  public static <T> T getProxyInstance(Class<T> clazz, String name, IntMap<MethodLambda> lambda){
    return getProxyInstance(clazz, ObjectMap.of(name, lambda), obj -> {});
  }
  
  public static <T> T getProxyInstance(Class<T> clazz, String name, IntMap<MethodLambda> lambda, Cons<T> afterHandle){
    return getProxyInstance(clazz, ObjectMap.of(name, lambda), afterHandle);
  }
  
  public static <T> T getProxyInstance(Class<T> clazz, ObjectMap<String, IntMap<MethodLambda>> overrideMethods){
    return getProxyInstance(clazz, overrideMethods, obj -> {});
  }
  
  public static <T> T getProxyInstance(Class<T> clazz, ObjectMap<String, IntMap<MethodLambda>> overrideMethods, Cons<T> afterHandle){
    return null;
  }
  
  public interface MethodLambda{
    Object invoke(Object object, Object[] args, Prov<Object> superMethod);
  }
  
  private static class MethodInterceptor{
  }
}
