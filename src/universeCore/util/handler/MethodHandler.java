package universeCore.util.handler;

import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class MethodHandler{
  public static <T> T invokeMethod(Object target, String method, Object... param) throws Throwable{
    Method m = target.getClass().getDeclaredMethod("method");
    m.setAccessible(true);
    return (T)m.invoke(param);
  }
  
  public static <T> T invokeNonException(Object target, String method, Object... param){
    try{
      return invokeMethod(target, method, param);
    }
    catch(Throwable ignore){
      return null;
    }
  }
}
