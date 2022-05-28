package universecore.util.classes;

import universecore.util.mods.ModInfo;

import java.io.File;

public abstract class BaseGeneratedClassLoader extends AbstractGeneratedClassLoader{
  protected final ModInfo mod;
  
  public BaseGeneratedClassLoader(ModInfo mod, File cacheFile, ClassLoader parent){
    super(cacheFile, parent);
    this.mod = mod;
  }
}
