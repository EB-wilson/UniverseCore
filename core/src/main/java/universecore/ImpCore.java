package universecore;

import universecore.util.AccessAndModifyHelper;
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
      e.printStackTrace();
      throw new RuntimeException("what? how do you do caused this error?");
    }
  }

  public static ClassHandlerFactory classes;
  public static AccessAndModifyHelper accessAndModifyHelper;
}
