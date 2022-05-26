package universecore;

import universecore.androidcore.AndroidFinalSetter;
import universecore.androidcore.classes.AndroidClassHandlerFactory;

public class SetPlatformImpl{
  public static void setImplements(){
    ImpCore.classes = new AndroidClassHandlerFactory();
    ImpCore.finalSetter = new AndroidFinalSetter();
  }
}
