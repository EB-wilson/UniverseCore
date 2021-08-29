package universeCore.util.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class MethodHandler{
  public static <T> T invokeMethod(Class<?> clazz, Object object, String method, Object... param) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{
    Method m = null;
    try{
      m = clazz.getDeclaredMethod("method");
    }
    catch(Throwable e){
      if(clazz.getSuperclass() == Object.class) throw new NoSuchMethodException();
      invokeMethod(clazz.getSuperclass(), object, method, param);
    }
  
    assert m != null;
    m.setAccessible(true);
    return (T)m.invoke(param);
  }
  
  public static <T> T invokeNonException(Object target, String method, Object... param){
    try{
      return invokeMethod(target.getClass(), target, method, param);
    }
    catch(Throwable ignore){
      return null;
    }
  }
}
