package universecore;

import mindustry.mod.Mod;
import universecore.desktopcore.DesktopAccessibleHelper;
import universecore.desktopcore.DesktopFieldAccessHelper;
import universecore.desktopcore.DesktopMethodInvokeHelper;
import universecore.desktopcore.handler.DesktopClassHandler;
import universecore.util.AccessibleHelper;
import universecore.util.FieldAccessHelper;
import universecore.util.MethodInvokeHelper;
import universecore.util.mods.IllegalModHandleException;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SetPlatformImpl{
  @SuppressWarnings("unchecked")
  public static void setImplements(){
    try{
      Class.forName("java.lang.Module");

      Class<? extends AccessibleHelper> accessible9Type =
          (Class<? extends AccessibleHelper>) Class.forName("universecore.desktop9core.DesktopAccessibleHelper9");
      Class<? extends FieldAccessHelper> fieldAccess9Type =
          (Class<? extends FieldAccessHelper>) Class.forName("universecore.desktop9core.DesktopFieldAccessHelper9");
      Class<? extends MethodInvokeHelper> methodInvoke9Type =
          (Class<? extends MethodInvokeHelper>) Class.forName("universecore.desktop9core.DesktopMethodInvokeHelper9");

      try{
        Constructor<? extends AccessibleHelper> acCstr = accessible9Type.getConstructor();
        Constructor<? extends FieldAccessHelper> faCstr = fieldAccess9Type.getConstructor();
        Constructor<? extends MethodInvokeHelper> miCstr = methodInvoke9Type.getConstructor();

        ImpCore.accessibleHelper = acCstr.newInstance();
        ImpCore.fieldAccessHelper = faCstr.newInstance();
        ImpCore.methodInvokeHelper = miCstr.newInstance();
      }catch(NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e){
        throw new RuntimeException(e);
      }
    }catch(ClassNotFoundException ignored){
      ImpCore.accessibleHelper = new DesktopAccessibleHelper();
      ImpCore.fieldAccessHelper = new DesktopFieldAccessHelper();
      ImpCore.methodInvokeHelper = new DesktopMethodInvokeHelper();
    }

    ImpCore.classes = modMain -> {
      try{
        if(!Mod.class.isAssignableFrom(modMain))
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
