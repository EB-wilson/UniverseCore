package universeCore.util.handler;

import arc.func.Cons;
import arc.func.Prov;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.util.Log;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

@SuppressWarnings("unchecked")
public class ProxyHandler{
  public static <T> T getProxyInstance(Class<T> type, MethodInterceptor inter){
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(type);
    enhancer.setCallback(inter);
    
    return (T) enhancer.create();
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
    T result = getProxyInstance(clazz, (object, method, args, proxyMethod) -> {
      return overrideMethods.get(method.getName()).get(args.length).invoke(object, args, () -> {
        try{
          return proxyMethod.invokeSuper(object, args);
        }catch(Throwable throwable){
          Log.err(throwable);
          return null;
        }
      });
    });
    
    afterHandle.get(result);
    return result;
  }
  
  public interface MethodLambda{
    Object invoke(Object object, Object[] args, Prov<Object> superMethod);
  }
}
