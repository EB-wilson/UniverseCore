package universecore.util.classes;

import universecore.util.mods.ModInfo;

public abstract class BaseGeneratedClassLoader extends AbstractGeneratedClassLoader{
  protected final ModInfo mod;

  protected static final JarList jarList = new JarList();
  
  public BaseGeneratedClassLoader(ModInfo mod, ClassLoader parent){
    super(jarList.getCacheFile(mod).file(), parent);
    this.mod = mod;
  }
}
