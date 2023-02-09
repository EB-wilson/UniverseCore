package universecore.desktopcore.classes;

import universecore.util.classes.BaseDynamicClassLoader;
import universecore.util.handler.MethodHandler;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;

public class DesktopDynamicClassLoader extends BaseDynamicClassLoader{
  private final HashMap<String, byte[]> classes = new HashMap<>();
  private final HashMap<String, Class<?>> loadedClass = new HashMap<>();
  private Class<?> currAccessor;

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
        if(currAccessor != null){
          ClassLoader loader = currAccessor.getClassLoader();
          ProtectionDomain domain = currAccessor.getProtectionDomain();

          res = MethodHandler.invokeDefault(ClassLoader.class, "defineClass0",
              loader,
              currAccessor,
              name,
              code, 0, code.length,
              domain,
              false,
              Modifier.PUBLIC,
              null
          );
          currAccessor = null;
        }
        else{
          res = defineClass(name, code, 0, code.length);
        }
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
  public void setAccessor(Class<?> accessor){
    currAccessor = accessor;
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
