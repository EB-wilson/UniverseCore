package universecore;

import mindustry.mod.Mod;
import universecore.desktopcore.DesktopAccessibleHelper;
import universecore.desktopcore.DesktopFieldAccessHelper;
import universecore.desktopcore.DesktopMethodInvokeHelper;
import universecore.desktopcore.handler.DesktopClassHandler;
import universecore.util.AccessibleHelper;
import universecore.util.FieldAccessHelper;
import universecore.util.MethodInvokeHelper;
import universecore.util.handler.ClassHandler;
import universecore.util.IllegalModHandleException;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SetPlatformImpl{
  private static Constructor<? extends ClassHandler> handlerCstr;

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
      Class<? extends ClassHandler> classHandler =
          (Class<? extends ClassHandler>) Class.forName("universecore.desktop9core.handler.DesktopClassHandler9");

      try{
        Constructor<? extends AccessibleHelper> acCstr = accessible9Type.getConstructor();
        Constructor<? extends FieldAccessHelper> faCstr = fieldAccess9Type.getConstructor();
        Constructor<? extends MethodInvokeHelper> miCstr = methodInvoke9Type.getConstructor();
        handlerCstr = classHandler.getConstructor(ModInfo.class);

        UncCore.accessibleHelper = acCstr.newInstance();
        UncCore.fieldAccessHelper = faCstr.newInstance();
        UncCore.methodInvokeHelper = miCstr.newInstance();
      }catch(NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e){
        throw new RuntimeException(e);
      }
    }catch(ClassNotFoundException ignored){
      UncCore.accessibleHelper = new DesktopAccessibleHelper();
      UncCore.fieldAccessHelper = new DesktopFieldAccessHelper();
      UncCore.methodInvokeHelper = new DesktopMethodInvokeHelper();

      try {
        handlerCstr = DesktopClassHandler.class.getConstructor(ModInfo.class);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    UncCore.classesFactory = modMain -> {
      try{
        if(!Mod.class.isAssignableFrom(modMain))
          throw new IllegalModHandleException("class was not a mod main class");

        ModInfo mod = ModGetter.getModWithClass((Class<? extends Mod>) modMain);
        if(mod == null)
          throw new IllegalModHandleException("mod with main class " + modMain + " was not found");

        ModGetter.checkModFormat(mod.file);

        return handlerCstr.newInstance(mod);
      }catch(IllegalModHandleException | InvocationTargetException | InstantiationException | IllegalAccessException e){
        throw new RuntimeException(e);
      }
    };
  }
}
