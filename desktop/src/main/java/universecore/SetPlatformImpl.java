package universecore;

import universecore.desktopcore.DesktopAccessAndModifyHelper;
import universecore.desktopcore.classes.DesktopClassHandlerFactory;

public class SetPlatformImpl{
  public static void setImplements(){
    ImpCore.classes = new DesktopClassHandlerFactory();
    ImpCore.accessAndModifyHelper = new DesktopAccessAndModifyHelper();
  }
}
