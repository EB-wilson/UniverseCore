package dynamilize.unc;

import dynamilize.DynamicObject;
import dynamilize.IVariable;
import universecore.ImpCore;

import java.lang.reflect.Field;

public class UncJavaFieldRef implements IVariable {
  private final String name;
  private final Class<?> owner;

  public UncJavaFieldRef(Field field){
    name = field.getName();
    owner = field.getDeclaringClass();
  }

  @Override
  public String name() {
    return name;
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

  @Override
  public boolean get(DynamicObject<?> dynamicObject, boolean b) {
    return false;
  }

  @Override
  public byte get(DynamicObject<?> dynamicObject, byte b) {
    return 0;
  }

  @Override
  public short get(DynamicObject<?> dynamicObject, short i) {
    return 0;
  }

  @Override
  public int get(DynamicObject<?> dynamicObject, int i) {
    return 0;
  }

  @Override
  public long get(DynamicObject<?> dynamicObject, long l) {
    return 0;
  }

  @Override
  public float get(DynamicObject<?> dynamicObject, float v) {
    return 0;
  }

  @Override
  public double get(DynamicObject<?> dynamicObject, double v) {
    return 0;
  }

  @Override
  public void set(DynamicObject<?> dynamicObject, boolean b) {

  }

  @Override
  public void set(DynamicObject<?> dynamicObject, byte b) {

  }

  @Override
  public void set(DynamicObject<?> dynamicObject, short i) {

  }

  @Override
  public void set(DynamicObject<?> dynamicObject, int i) {

  }

  @Override
  public void set(DynamicObject<?> dynamicObject, long l) {

  }

  @Override
  public void set(DynamicObject<?> dynamicObject, float v) {

  }

  @Override
  public void set(DynamicObject<?> dynamicObject, double v) {

  }
}
