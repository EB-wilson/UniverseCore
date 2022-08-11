package universecore.desktopcore.classes;

import universecore.util.classes.BaseDynamicClassLoader;

import java.util.HashMap;

public class DesktopDynamicClassLoader extends BaseDynamicClassLoader{
  private final HashMap<String, byte[]> classes = new HashMap<>();
  private final HashMap<String, Class<?>> loadedClass = new HashMap<>();
  public DesktopDynamicClassLoader(ClassLoader parent){
    super(parent);
    reset();
  }
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException{
    Class<?> res = loadedClass.get(name);
    if(res != null) return res;

    try{
      return super.findClass(name);
    }catch(ClassNotFoundException ignored){
      byte[] code = classes.get(name);
      if(code != null){
        res = defineClass(name, code, 0, code.length);
        loadedClass.put(name, res);
        return res;
      }
      throw new ClassNotFoundException("class not found: " + name);
    }
  }
  
  @Override
  public void reset(){
    classes.clear();
  }

  @Override
  public void declareClass(String name, byte[] byteCode){
    classes.put(name, byteCode);
  }

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
    Class<?> res;
    try{
       res = super.loadClass(name);
    }catch(ClassNotFoundException ignored){
       res = findClass(name);
    }

    if(resolve) resolveClass(res);

    return res;
  }
}
