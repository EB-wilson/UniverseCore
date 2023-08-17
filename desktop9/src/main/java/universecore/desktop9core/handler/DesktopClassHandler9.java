package universecore.desktop9core.handler;

import mindustry.Vars;
import mindustry.mod.Mod;
import universecore.desktop9core.classes.DesktopDynamicClassLoader9;
import universecore.desktop9core.classes.DesktopGeneratedClassLoader9;
import universecore.desktopcore.handler.DesktopClassHandler;
import universecore.util.handler.ClassHandler;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

public class DesktopClassHandler9 extends DesktopClassHandler {
  public DesktopClassHandler9(ModInfo mod) {
    super(mod);
  }

  @Override
  protected void initLoaders() {
    generatedLoader = new DesktopGeneratedClassLoader9(mod, Vars.mods.mainLoader());
    dynamicLoader = new DesktopDynamicClassLoader9(generatedLoader);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ClassHandler newInstance(Class<?> modMain){
    if(!Mod.class.isAssignableFrom(modMain))
      throw new IllegalArgumentException("require class is child of Mod");

    ModInfo mod = ModGetter.getModWithClass((Class<? extends Mod>) modMain);
    if(mod == null)
      throw new IllegalArgumentException("mod that inputted main class was not found");

    return new DesktopClassHandler9(mod);
  }
}
