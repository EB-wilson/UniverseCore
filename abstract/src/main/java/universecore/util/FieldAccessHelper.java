package universecore.util;

public interface FieldAccessHelper{
  void set(Object object, String field, Object value);

  void setStatic(Class<?> clazz, String field, Object value);

  <T> T get(Object object, String field);

  <T> T getStatic(Class<?> clazz, String field);
}
