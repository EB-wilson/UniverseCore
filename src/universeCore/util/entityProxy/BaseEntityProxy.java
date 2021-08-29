package universeCore.util.entityProxy;

import arc.func.Cons;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Entityc;
import universeCore.util.handler.ProxyHandler;
import universeCore.util.handler.ProxyHandler.MethodLambda;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BaseEntityProxy{
  public final static MethodLambda defaults = (object, args, superMethod) -> superMethod.get();
  public final ObjectMap<String, IntMap<MethodLambda>> methodOverrideList;
  
  public final Object object;
  
  public Cons<Entityc> afterInit = e -> {};
  
  public BaseEntityProxy(Object object){
    if(proxyType().isInstance(object))
      throw new RuntimeException("class \"" + proxyType().getName() + "\" can not to proxy a class " + object.getClass().getName() + " instance");
    
    this.object = object;
    methodOverrideList = new ObjectMap<>();
    
    Seq<Method> allSuperMethod = new Seq<>();
    allSuperMethod.addAll(proxyType().getMethods());
    
    for(Method method: allSuperMethod){
      int paramLength = method.getParameterCount();
      
      String name = method.getName();
  
      try{
        Method proxyGetter = getClass().getMethod(name, method.getParameterTypes());
        IntMap<MethodLambda> map = methodOverrideList.get(name);
        if(map == null){
          map = new IntMap<>();
          methodOverrideList.put(name, map);
        }
        
        map.put(method.getParameterCount(), (MethodLambda)proxyGetter.invoke(this, new Object[paramLength]));
        
      }catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
        Log.err(e);
      }
    }
  }
  
  public Object newProxyInstance(){
    return ProxyHandler.getProxyInstance(proxyType(), methodOverrideList, e -> afterInit.get((Entityc)e));
  }
  
  public Class<?> proxyType(){
    return Entityc.class;
  }
  
  public abstract MethodLambda update();
  public abstract MethodLambda write(Writes var1);
  public abstract MethodLambda read(Reads var1);
}
