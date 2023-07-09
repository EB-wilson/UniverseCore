package universecore.desktop9core.handler;

import mindustry.Vars;
import universecore.desktop9core.classes.DesktopDynamicClassLoader9;
import universecore.desktop9core.classes.DesktopGeneratedClassLoader9;
import universecore.desktopcore.handler.DesktopClassHandler;
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
}
