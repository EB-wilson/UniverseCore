package universecore.desktopcore;

import universecore.util.AccessibleHelper;

import java.lang.reflect.AccessibleObject;

public class DesktopAccessibleHelper implements AccessibleHelper {
  @Override
  public void makeAccessible(AccessibleObject object) {
    object.setAccessible(true);
  }

  @Override
  public void makeClassAccessible(Class<?> clazz) {
    //no action
  }
}
