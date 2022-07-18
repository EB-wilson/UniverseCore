package universecore.util.classes;

import java.io.File;

public abstract class AbstractDynamicClassLoader extends AbstractFileClassLoader{
  public AbstractDynamicClassLoader(File file, ClassLoader parent){
    super(file, parent);
  }

  public abstract void reset();
}
