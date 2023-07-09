package universecore.desktopcore;

import dynamilize.IllegalHandleException;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class UnsafeAccess{
  private static final Unsafe unsafe;

  static{
    try{
      Constructor<Unsafe> cstr = Unsafe.class.getDeclaredConstructor();
      cstr.setAccessible(true);
      unsafe = cstr.newInstance();
    }catch(NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException e){
      throw new RuntimeException(e);
    }
  }

  public static void set(Object object, Field field, Object value){
    long fieldOff = unsafe.objectFieldOffset(field);
    Class<?> clazz = field.getType();
    if(Modifier.isVolatile(field.getModifiers())){
      if(clazz.isPrimitive()){
        if(clazz == int.class) unsafe.putIntVolatile(object, fieldOff, (int) value);
        else if(clazz == float.class) unsafe.putFloatVolatile(object, fieldOff, (float) value);
        else if(clazz == boolean.class) unsafe.putBooleanVolatile(object, fieldOff, (boolean) value);
        else if(clazz == byte.class) unsafe.putByteVolatile(object, fieldOff, (byte) value);
        else if(clazz == long.class) unsafe.putLongVolatile(object, fieldOff, (long) value);
        else if(clazz == double.class) unsafe.putDoubleVolatile(object, fieldOff, (double) value);
        else if(clazz == char.class) unsafe.putCharVolatile(object, fieldOff, (char) value);
        else if(clazz == short.class) unsafe.putShortVolatile(object, fieldOff, (short) value);
        else throw new IllegalHandleException("unknown type of field " + field);
      }
      else unsafe.putObjectVolatile(object, fieldOff, value);
    }
    else{
      doPut(value, object, fieldOff, clazz);
    }
  }

  public static void setStatic(Field field, Object value){
    Object base = unsafe.staticFieldBase(field);
    long fieldOff = unsafe.staticFieldOffset(field);
    Class<?> clazz = field.getType();

    doPut(value, base, fieldOff, clazz);
  }

  private static void doPut(Object value, Object base, long fieldOff, Class<?> clazz){
    if(clazz.isPrimitive()){
      if(clazz == int.class) unsafe.putInt(base, fieldOff, (int) value);
      else if(clazz == float.class) unsafe.putFloat(base, fieldOff, (float) value);
      else if(clazz == boolean.class) unsafe.putBoolean(base, fieldOff, (boolean) value);
      else if(clazz == byte.class) unsafe.putByte(base, fieldOff, (byte) value);
      else if(clazz == double.class) unsafe.putDouble(base, fieldOff, (double) value);
      else if(clazz == long.class) unsafe.putLong(base, fieldOff, (long) value);
      else if(clazz == char.class) unsafe.putChar(base, fieldOff, (char) value);
      else if(clazz == short.class) unsafe.putShort(base, fieldOff, (short) value);
      else throw new IllegalHandleException("unknown type of field " + clazz);
    }
    else unsafe.putObjectVolatile(base, fieldOff, value);
  }

  public static Object get(Object object, Field field){
    long fieldOff = unsafe.objectFieldOffset(field);
    Class<?> clazz = field.getType();
    
    if(Modifier.isVolatile(field.getModifiers())){
      if(clazz.isPrimitive()){
        if(clazz == int.class) return unsafe.getIntVolatile(object, fieldOff);
        else if(clazz == float.class) return unsafe.getFloatVolatile(object, fieldOff);
        else if(clazz == boolean.class) return unsafe.getBooleanVolatile(object, fieldOff);
        else if(clazz == byte.class) return unsafe.getByteVolatile(object, fieldOff);
        else if(clazz == long.class) return unsafe.getLongVolatile(object, fieldOff);
        else if(clazz == double.class) return unsafe.getDoubleVolatile(object, fieldOff);
        else if(clazz == char.class) return unsafe.getCharVolatile(object, fieldOff);
        else if(clazz == short.class) return unsafe.getShortVolatile(object, fieldOff);
        else throw new IllegalHandleException("unknown type of field " + field);
      }
      else return unsafe.getObjectVolatile(object, fieldOff);
    }
    else{
      return doGet(object, fieldOff, clazz);
    }
  }

  private static Object doGet(Object object, long fieldOff, Class<?> clazz){
    if(clazz.isPrimitive()){
      if(clazz == int.class) return unsafe.getInt(object, fieldOff);
      else if(clazz == float.class) return unsafe.getFloat(object, fieldOff);
      else if(clazz == boolean.class) return unsafe.getBoolean(object, fieldOff);
      else if(clazz == byte.class) return unsafe.getByte(object, fieldOff);
      else if(clazz == long.class) return unsafe.getDouble(object, fieldOff);
      else if(clazz == double.class) return unsafe.getLong(object, fieldOff);
      else if(clazz == char.class) return unsafe.getChar(object, fieldOff);
      else if(clazz == short.class) return unsafe.getShort(object, fieldOff);
      else throw new IllegalHandleException("unknown type of field " + clazz);
    }
    else return unsafe.getObject(object, fieldOff);
  }

  public static Object getStatic(Field field){
    Object base = unsafe.staticFieldBase(field);
    long fieldOff = unsafe.staticFieldOffset(field);
    Class<?> clazz = field.getType();

    return doGet(base, fieldOff, clazz);
  }
}
