package universecore;

import mindustry.mod.Mod;
import universecore.desktopcore.DesktopAccessAndModifyHelper;
import universecore.desktopcore.handler.DesktopClassHandler;
import universecore.util.mods.IllegalModHandleException;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

public class SetPlatformImpl{
  @SuppressWarnings("unchecked")
  public static void setImplements(){
    ImpCore.accessAndModifyHelper = new DesktopAccessAndModifyHelper();
    ImpCore.classes = modMain -> {
      try{
        if(!modMain.isAssignableFrom(Mod.class))
          throw new IllegalModHandleException("class was not a mod main class");

        ModInfo mod = ModGetter.getModWithClass((Class<? extends Mod>) modMain);
        if(mod == null)
          throw new IllegalModHandleException("mod with main class " + modMain + " was not found");

        ModGetter.checkModFormat(mod.file);
        return new DesktopClassHandler(mod);
      }catch(IllegalModHandleException e){
        throw new RuntimeException(e);
      }
    };
  }
}
