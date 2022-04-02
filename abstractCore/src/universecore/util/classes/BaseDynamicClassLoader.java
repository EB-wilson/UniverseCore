package universecore.util.classes;

import arc.Core;
import arc.files.Fi;

public abstract class BaseDynamicClassLoader extends AbstractFileClassLoader{
  public static final Fi jarFileCache = Core.files.cache("tempGenerate.jar");
  
  public BaseDynamicClassLoader(ClassLoader parent){
    super(jarFileCache.file(), parent);
  }
  
  public abstract void defineClass(String name, byte[] code);
  
  @Override
  public final Class<?> loadClass(String name, Class<?> neighbor) throws ClassNotFoundException{
    throw new IllegalArgumentException("dynamic classLoader can not load class with a neighbor class");
  }
  
  protected abstract ClassLoader getVMLoader();
  
  public abstract void reset();
}
