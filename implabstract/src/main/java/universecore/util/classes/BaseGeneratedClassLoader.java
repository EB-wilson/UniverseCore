package universecore.util.classes;

import universecore.util.mods.ModInfo;

public abstract class BaseGeneratedClassLoader extends AbstractGeneratedClassLoader{
  protected final ModInfo mod;

  public BaseGeneratedClassLoader(ModInfo mod, ClassLoader parent){
    super(JarList.inst().getCacheFile(mod).file(), parent);
    this.mod = mod;
  }
}
