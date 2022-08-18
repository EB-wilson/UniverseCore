package dynamilize.classmaker;

import dynamilize.IllegalHandleException;

import java.util.HashMap;
import java.util.Map;

public class BaseClassLoader extends ClassLoader implements ByteClassLoader{
  protected Map<String, byte[]> bytecodes = new HashMap<>();
  protected Map<String, Class<?>> classMap = new HashMap<>();

  public BaseClassLoader(ClassLoader parent){
    super(parent);
  }

  @Override
  public void declareClass(String name, byte[] byteCode){
    if(bytecodes.put(name, byteCode) != null)
      throw new IllegalHandleException("cannot declare class with same name twice");
  }

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
    return super.loadClass(name, resolve);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException{
    try{
      return super.findClass(name);
    }catch(ClassNotFoundException e){
      Class<?> result = classMap.computeIfAbsent(name, n -> {
        byte[] byteCode = bytecodes.get(n);
        if(byteCode == null) return null;

        return defineClass(n, byteCode, 0, byteCode.length);
      });

      if(result == null)
        throw new ClassNotFoundException("class " + name + " was not declared and not found a existed.");

      return result;
    }
  }
}
