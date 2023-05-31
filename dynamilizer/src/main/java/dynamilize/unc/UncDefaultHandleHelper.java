package dynamilize.unc;

import dynamilize.DataPool;
import dynamilize.IFunctionEntry;
import dynamilize.IVariable;
import dynamilize.JavaHandleHelper;
import universecore.ImpCore;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UncDefaultHandleHelper implements JavaHandleHelper {
  @Override
  public void makeAccess(Object object) {
    if (object instanceof AccessibleObject obj){
      ImpCore.accessibleHelper.makeAccessible(obj);
    }
    else if (object instanceof Class<?> clazz){
      ImpCore.accessibleHelper.makeClassAccessible(clazz);
    }
    else throw new IllegalArgumentException("given obj unusable, it must be AccessibleObject or Class");
  }

  @Override
  public IVariable genJavaVariableRef(Field field, DataPool targetPool) {
    return new UncJavaFieldRef(field);
  }

  @Override
  public IFunctionEntry genJavaMethodRef(Method method, DataPool targetPool) {
    return new UncJavaMethodRef(method, targetPool);
  }
}
