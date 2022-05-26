package universecore.desktopcore;

import universecore.ImpCore;
import universecore.desktopcore.classes.DesktopClassHandlerFactory;

public class SetPlatformImpl{
  public static void setImplements(){
    ImpCore.classes = new DesktopClassHandlerFactory();
    ImpCore.finalSetter = new DesktopFinalSetter();
  }
}
