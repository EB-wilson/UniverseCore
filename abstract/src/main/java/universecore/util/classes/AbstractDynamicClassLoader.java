package universecore.util.classes;

import java.io.File;

public abstract class AbstractDynamicClassLoader extends AbstractFileClassLoader{
  public AbstractDynamicClassLoader(File file, ClassLoader parent){
    super(file, parent);
  }

  public abstract void defineClass(String name, byte[] code);

  @Override
  public final Class<?> loadClass(String name, Class<?> neighbor){
    throw new IllegalArgumentException("dynamic classLoader can not load class with a neighbor class");
  }

  protected abstract ClassLoader getVMLoader();

  public abstract void reset();
}
