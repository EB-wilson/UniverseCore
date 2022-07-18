package universecore.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface AccessAndModifyHelper{
  void set(Object object, Field field, Object value);

  void setStatic(Class<?> clazz, Field field, Object value);

  void setAccessible(Field field);

  void setAccessible(Method method);

  <T> void setAccessible(Constructor<T> cstr);

  default void setAccessible(AccessibleObject obj){
    if(obj instanceof Field f) setAccessible(f);
    else if(obj instanceof Method m) setAccessible(m);
    else if(obj instanceof Constructor c) setAccessible(c);
    else throw new IllegalAccessError("unknown type as " + obj.getClass());
  }
}