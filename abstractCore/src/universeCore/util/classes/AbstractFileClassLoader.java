package universeCore.util.classes;

import java.io.File;

public abstract class AbstractFileClassLoader extends ClassLoader{
  protected final File file;
  protected ClassLoader loader;
  
  public AbstractFileClassLoader(File file, ClassLoader parent){
    super(parent);
    this.file = file;
  }
  
  public File getFile(){
    return file;
  }
  
  public void loadJar(){
    loader = getVMLoader();
  }
  
  protected abstract ClassLoader getVMLoader();
  
  public abstract byte[] merge(byte[] other);
  
  public abstract Class<?> loadClass(String name, Class<?> neighbor) throws ClassNotFoundException;
  
  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException{
    if(!fileExist()) throw new ClassNotFoundException(name);
    if(loader == null) loader = getVMLoader();
    return loader.loadClass(name);
  }
  
  public abstract void writeFile(byte[] data);
  
  public boolean fileExist(){
    return file.exists();
  }
}
