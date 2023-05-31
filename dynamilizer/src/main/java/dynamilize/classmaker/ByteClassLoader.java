package dynamilize.classmaker;

public interface ByteClassLoader{
  void declareClass(String name, byte[] byteCode);

  Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException;
}
