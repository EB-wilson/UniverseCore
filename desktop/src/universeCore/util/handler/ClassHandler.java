package universeCore.util.handler;

import universeCore.util.classMakers.ByteCodeClassLoader;
import universeCore.util.classMakers.UncClass;
import universeCore.util.proxy.BaseProxy;
import universeCore.util.proxy.DesktopProxy;

public class ClassHandler{
  private static final ByteCodeClassLoader loader = new ByteCodeClassLoader();
  
  public static Class<?> load(UncClass clazz){
    loader.assignClasses(clazz.name, clazz.getByteCode());
    try{
      return loader.loadClass(clazz.name);
    }catch(ClassNotFoundException e){
      throw new RuntimeException(e);
    }
  }
  
  public static <T> BaseProxy<T> getProxy(Class<T> clazz){
    return new DesktopProxy<>(clazz);
  }
}
