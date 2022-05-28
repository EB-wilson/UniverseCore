package universecore.util.classes;

import arc.struct.ObjectMap;
import mindustry.mod.Mod;
import universecore.util.handler.ClassHandler;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

public abstract class BaseClassHandlerFactory implements ClassHandlerFactory{
  private final ObjectMap<Class<?>, ClassHandler> handlers = new ObjectMap<>();

  @SuppressWarnings("unchecked")
  @Override
  public final ClassHandler getHandler(Class<?> caller){
    if(!Mod.class.isAssignableFrom(caller)) throw new IllegalArgumentException("caller must be ModMain class, given: " + caller);

    return handlers.get(caller, () -> generate(ModGetter.getModWithClass((Class<? extends Mod>) caller), caller.getClassLoader()));
  }

  protected abstract ClassHandler generate(ModInfo modInfo, ClassLoader modLoader);
}
