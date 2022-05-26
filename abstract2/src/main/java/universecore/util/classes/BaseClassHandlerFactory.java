package universecore.util.classes;

import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.mod.Mod;
import universecore.util.handler.ClassHandler;

public abstract class BaseClassHandlerFactory implements ClassHandlerFactory{
  private final ObjectMap<Class<?>, ClassHandler> handlers = new ObjectMap<>();

  @SuppressWarnings("unchecked")
  @Override
  public final ClassHandler getHandler(Class<?> caller){
    if(!Mod.class.isAssignableFrom(caller)) throw new IllegalArgumentException("caller must be ModMain class, given: " + caller);
    return handlers.get(caller, () -> generate(new JarList.ModInfo(Vars.mods.getMod((Class<? extends Mod>) caller).file), caller.getClassLoader()));
  }

  protected abstract ClassHandler generate(JarList.ModInfo modInfo, ClassLoader modLoader);
}
