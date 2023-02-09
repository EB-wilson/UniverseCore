package dynamilize.classmaker;

public interface ByteClassLoader{
  /**特殊的操作，用于将当前（或者说此次）加载的类扩展的的超类设为可访问，这个行为主要用于应对非开放内部类*/
  void setAccessor(Class<?> accessor);

  void declareClass(String name, byte[] byteCode);

  Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException;
}
