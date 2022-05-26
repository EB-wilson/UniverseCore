package universecore.desktopcore.classes;

import universecore.desktopcore.handler.DesktopClassHandler;
import universecore.util.classes.BaseClassHandlerFactory;
import universecore.util.classes.JarList;
import universecore.util.handler.ClassHandler;

public class DesktopClassHandlerFactory extends BaseClassHandlerFactory{
  @Override
  protected ClassHandler generate(JarList.ModInfo modInfo, ClassLoader modLoader){
    return new DesktopClassHandler(modInfo, modLoader);
  }
}
