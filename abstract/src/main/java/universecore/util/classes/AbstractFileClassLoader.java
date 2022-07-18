package universecore.util.classes;

import dynamilize.classmaker.ByteClassLoader;

import java.io.File;

public abstract class AbstractFileClassLoader extends ClassLoader implements ByteClassLoader{
  protected final File file;
  
  public AbstractFileClassLoader(File file, ClassLoader parent){
    super(parent);
    this.file = file;
  }
  
  public File getFile(){
    return file;
  }

  @Override
  public abstract void declareClass(String name, byte[] byteCode);

  @Override
  public abstract Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException;
}
