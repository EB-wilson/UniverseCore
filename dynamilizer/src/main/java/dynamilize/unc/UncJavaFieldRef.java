package dynamilize.unc;

import dynamilize.DynamicObject;
import dynamilize.IVariable;
import universecore.ImpCore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class UncJavaFieldRef implements IVariable {
  private final String name;
  private final Class<?> owner;
  private final boolean isFinal;

  public UncJavaFieldRef(Field field){
    name = field.getName();
    owner = field.getDeclaringClass();
    isFinal = Modifier.isFinal(field.getModifiers());
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean isConst() {
    return isFinal;
  }

  @Override
  public <T> T get(DynamicObject<?> obj) {
    if (!owner.isAssignableFrom(obj.getClass()))
      throw new ClassCastException(obj.getClass() + " can not be cast to " + owner);

    return ImpCore.fieldAccessHelper.get(obj, name);
  }

  @Override
  public void set(DynamicObject<?> obj, Object value) {
    if (!owner.isAssignableFrom(obj.getClass()))
      throw new ClassCastException(obj.getClass() + " can not be cast to " + owner);

    ImpCore.fieldAccessHelper.set(obj, name, value);
  }
}
