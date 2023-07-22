package universecore.desktopcore.classes;

import universecore.util.classes.BaseDynamicClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;

public class DesktopDynamicClassLoader extends BaseDynamicClassLoader{
  private static final Object unsafe;
  private static final Method defineClass;

  static{
    try{
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      Method def;
      try {
        def = unsafeClass.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
      } catch (NoSuchMethodException e){
        def = null;
      }
      defineClass = def;
      Constructor<?> cstr = unsafeClass.getDeclaredConstructor();
      cstr.setAccessible(true);
      unsafe = cstr.newInstance();
    }catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException |
           ClassNotFoundException e){
      throw new RuntimeException(e);
    }
  }

  private final HashMap<String, byte[]> classes = new HashMap<>();
  private final HashMap<String, Class<?>> loadedClass = new HashMap<>();

  public DesktopDynamicClassLoader(ClassLoader parent){
    super(parent);
    reset();
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    return findClass(name, (Class<?>) null);
  }

  protected Class<?> findClass(String name, Class<?> accessor) throws ClassNotFoundException{
    Class<?> res = loadedClass.get(name);
    if(res != null) return res;

    try{
      return super.findClass(name);
    }catch(ClassNotFoundException ignored){
      byte[] code = classes.get(name);
      if(code != null){
        res = accessor != null? defineClass(name, code, accessor)
            : defineClass(name, code, 0, code.length);
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
  public Class<?> loadClass(String name, Class<?> accessor, boolean resolve) throws ClassNotFoundException{
    Class<?> res;
    try {
      res = getParent().loadClass(name);
    }catch (ClassNotFoundException ignored){
      res = findClass(name, accessor);
    }

    if(resolve) resolveClass(res);

    return res;
  }

  protected Class<?> defineClass(String name, byte[] bytes, Class<?> accessor){
    try {
      return (Class<?>) defineClass.invoke(unsafe, name, bytes, 0, bytes.length, accessor.getClassLoader(), null);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
