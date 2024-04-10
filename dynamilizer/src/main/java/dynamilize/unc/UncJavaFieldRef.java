package dynamilize.unc;

import dynamilize.DynamicObject;
import dynamilize.IVariable;
import universecore.UncCore;

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
  public void init(DynamicObject<?> object) { /*no action*/ }

  @Override
  public <T> T get(DynamicObject<?> obj) {
    if (!owner.isAssignableFrom(obj.getClass()))
      throw new ClassCastException(obj.getClass() + " can not be cast to " + owner);

    return UncCore.fieldAccessHelper.get(obj, name);
  }

  @Override
  public void set(DynamicObject<?> obj, Object value) {
    if (!owner.isAssignableFrom(obj.getClass()))
      throw new ClassCastException(obj.getClass() + " can not be cast to " + owner);

    UncCore.fieldAccessHelper.set(obj, name, value);
  }

  @Override
  public boolean get(DynamicObject<?> dynamicObject, boolean b) {
    return UncCore.fieldAccessHelper.getBoolean(dynamicObject, name);
  }

  @Override
  public byte get(DynamicObject<?> dynamicObject, byte b) {
    return UncCore.fieldAccessHelper.getByte(dynamicObject, name);
  }

  @Override
  public short get(DynamicObject<?> dynamicObject, short i) {
    return UncCore.fieldAccessHelper.getShort(dynamicObject, name);
  }

  @Override
  public int get(DynamicObject<?> dynamicObject, int i) {
    return UncCore.fieldAccessHelper.getInt(dynamicObject, name);
  }

  @Override
  public long get(DynamicObject<?> dynamicObject, long l) {
    return UncCore.fieldAccessHelper.getLong(dynamicObject, name);
  }

  @Override
  public float get(DynamicObject<?> dynamicObject, float v) {
    return UncCore.fieldAccessHelper.getFloat(dynamicObject, name);
  }

  @Override
  public double get(DynamicObject<?> dynamicObject, double v) {
    return UncCore.fieldAccessHelper.getDouble(dynamicObject, name);
  }

  @Override
  public void set(DynamicObject<?> dynamicObject, boolean b) {
    UncCore.fieldAccessHelper.set(dynamicObject, name, b);
  }

  @Override
  public void set(DynamicObject<?> dynamicObject, byte b) {
    UncCore.fieldAccessHelper.set(dynamicObject, name, b);
  }

  @Override
  public void set(DynamicObject<?> dynamicObject, short s) {
    UncCore.fieldAccessHelper.set(dynamicObject, name, s);
  }

  @Override
  public void set(DynamicObject<?> dynamicObject, int i) {
    UncCore.fieldAccessHelper.set(dynamicObject, name, i);
  }

  @Override
  public void set(DynamicObject<?> dynamicObject, long l) {
    UncCore.fieldAccessHelper.set(dynamicObject, name, l);
  }

  @Override
  public void set(DynamicObject<?> dynamicObject, float f) {
    UncCore.fieldAccessHelper.set(dynamicObject, name, f);

  }

  @Override
  public void set(DynamicObject<?> dynamicObject, double d) {
    UncCore.fieldAccessHelper.set(dynamicObject, name, d);
  }
}
