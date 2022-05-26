package universecore.util;

import java.lang.reflect.Field;

public interface FinalSetter{
  void set(Object object, Field field, Object value);

  void setStatic(Class<?> clazz, Field field, Object value);
}