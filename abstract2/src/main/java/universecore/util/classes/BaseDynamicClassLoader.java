package universecore.util.classes;

import arc.Core;
import arc.files.Fi;

public abstract class BaseDynamicClassLoader extends AbstractDynamicClassLoader{
  public static final Fi jarFileCache = Core.files.cache("tempGenerate.jar");
  
  public BaseDynamicClassLoader(ClassLoader parent){
    super(jarFileCache.file(), parent);
  }
}
