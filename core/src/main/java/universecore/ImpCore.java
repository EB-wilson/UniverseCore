package universecore;

import universecore.util.FieldAccessHelper;
import universecore.util.MethodInvokeHelper;
import universecore.util.handler.ClassHandlerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ImpCore{
  static{
    try{
      Class<?> implClass = Class.forName("universecore.SetPlatformImpl");
      Method call = implClass.getMethod("setImplements");
      call.invoke(null);
    }catch(ClassNotFoundException|NoSuchMethodException|IllegalAccessException|InvocationTargetException e){
      StringBuilder trace = new StringBuilder();

      Throwable curr = e;
      while(curr != null){
        for(StackTraceElement element: curr.getStackTrace()){
          trace.append("    at ").append(element).append("\n");
        }
        curr = curr.getCause();
        if(curr != null) trace.append("Caused by: ").append(curr).append("\n");
      }

      throw new RuntimeException("what? how do you do caused this error? \nstack trace: "
          + e + "\n"
          + trace);
    }
  }

  public static ClassHandlerFactory classes;
  public static FieldAccessHelper fieldAccessHelper;
  public static MethodInvokeHelper methodInvokeHelper;
}
