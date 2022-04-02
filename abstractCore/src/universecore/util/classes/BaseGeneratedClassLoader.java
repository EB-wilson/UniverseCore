package universecore.util.classes;

import java.io.File;

public abstract class BaseGeneratedClassLoader extends AbstractFileClassLoader{
  protected final JarList.ModInfo mod;
  
  public BaseGeneratedClassLoader(JarList.ModInfo mod, File cacheFile, ClassLoader parent){
    super(cacheFile, parent);
    this.mod = mod;
  }
}
