package universecore.util.classes;

import java.io.File;

public abstract class AbstractGeneratedClassLoader extends AbstractFileClassLoader{
  public AbstractGeneratedClassLoader(File file, ClassLoader parent){
    super(file, parent);
  }
}
