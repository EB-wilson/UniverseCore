package universeCore.util.handler;


import universeCore.util.DexLoaderFactory;
import universeCore.util.classMakers.UncClass;
import universeCore.util.proxy.AndroidProxy;
import universeCore.util.proxy.BaseProxy;

public class ClassHandler{
  private static final DexLoaderFactory.AsClassDexLoader dexLoader = DexLoaderFactory.generateClassLoader();
  
  public static Class<?> load(UncClass clazz){
    return dexLoader.defineClass(clazz.name, clazz.getByteCode());
  }
  
  public static <T> BaseProxy<T> getProxy(Class<T> clazz){
    return new AndroidProxy<>(clazz);
  }
}
