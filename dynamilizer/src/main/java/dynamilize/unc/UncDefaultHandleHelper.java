package dynamilize.unc;

import dynamilize.IFunctionEntry;
import dynamilize.IVariable;
import dynamilize.JavaHandleHelper;
import universecore.UncCore;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UncDefaultHandleHelper implements JavaHandleHelper {
  @Override
  public void makeAccess(Object object) {
    if (object instanceof AccessibleObject obj){
      UncCore.accessibleHelper.makeAccessible(obj);
    }
    else if (object instanceof Class<?> clazz){
      UncCore.accessibleHelper.makeClassAccessible(clazz);
    }
    else throw new IllegalArgumentException("given obj unusable, it must be AccessibleObject or Class");
  }

  @Override
  public IVariable genJavaVariableRef(Field field) {
    return new UncJavaFieldRef(field);
  }

  @Override
  public IFunctionEntry genJavaMethodRef(Method method) {
    return new UncJavaMethodRef(method);
  }
}
