package universecore.androidcore.classes;

import universecore.androidcore.handler.AndroidClassHandler;
import universecore.util.mods.ModInfo;
import universecore.util.classes.BaseClassHandlerFactory;
import universecore.util.handler.ClassHandler;

public class AndroidClassHandlerFactory extends BaseClassHandlerFactory{
  @Override
  protected ClassHandler generate(ModInfo modInfo, ClassLoader modLoader){
    try{
      return new AndroidClassHandler(modInfo, modLoader);
    }catch(ClassNotFoundException|NoSuchMethodException e){
      throw new RuntimeException(e);
    }
  }
}
