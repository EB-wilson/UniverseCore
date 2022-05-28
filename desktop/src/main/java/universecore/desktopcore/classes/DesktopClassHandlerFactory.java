package universecore.desktopcore.classes;

import universecore.desktopcore.handler.DesktopClassHandler;
import universecore.util.mods.ModInfo;
import universecore.util.classes.BaseClassHandlerFactory;
import universecore.util.handler.ClassHandler;

public class DesktopClassHandlerFactory extends BaseClassHandlerFactory{
  @Override
  protected ClassHandler generate(ModInfo modInfo, ClassLoader modLoader){
    return new DesktopClassHandler(modInfo, modLoader);
  }
}
