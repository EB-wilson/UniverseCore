package universecore.nativeImpl;

import java.lang.reflect.Field;

public class FieldHandlerImpl{
  private static native void setInt(Field field, Object object, int value);

  private static native void setFloat(Field field, Object object, float value);

  private static native void setBoolean(Field field, Object object, boolean value);

  private static native void setLong(Field field, Object object, long value);

  private static native void setDouble(Field field, Object object, double value);

  private static native void setShort(Field field, Object object, short value);

  private static native void setByte(Field field, Object object, byte value);

  private static native void setObject(Field field, Object object, Object value);
}
