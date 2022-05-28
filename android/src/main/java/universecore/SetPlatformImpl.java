package universecore;

import universecore.androidcore.AndroidAccessAndModifyHelper;
import universecore.androidcore.classes.AndroidClassHandlerFactory;

public class SetPlatformImpl{
  public static void setImplements(){
    ImpCore.classes = new AndroidClassHandlerFactory();
    ImpCore.accessAndModifyHelper = new AndroidAccessAndModifyHelper();
  }
}
