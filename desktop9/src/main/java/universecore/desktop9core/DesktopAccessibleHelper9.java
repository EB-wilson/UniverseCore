package universecore.desktop9core;

import dynamilize.DynamicMaker;
import universecore.util.AccessibleHelper;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DesktopAccessibleHelper9 implements AccessibleHelper {
  @Override
  public void makeAccessible(AccessibleObject object) {
    if (object instanceof Method method){
      Demodulator.checkAndMakeModuleOpen(method.getReturnType().getModule(), method.getReturnType(), DynamicMaker.class.getModule());

      for (Class<?> type : method.getParameterTypes()) {
        Demodulator.checkAndMakeModuleOpen(type.getModule(), type, DynamicMaker.class.getModule());
      }

      method.setAccessible(true);
    }
    else if (object instanceof Field field){
      Demodulator.checkAndMakeModuleOpen(field.getType().getModule(), field.getType(), DynamicMaker.class.getModule());

      field.setAccessible(true);
    }
  }

  @Override
  public void makeClassAccessible(Class<?> clazz) {
    Demodulator.checkAndMakeModuleOpen(clazz.getModule(), clazz, DynamicMaker.class.getModule());
  }
}
