package universeCore.util.classMakers;

import java.util.HashMap;

public class ByteCodeClassLoader extends ClassLoader{
  private final HashMap<String, byte[]> classesMap = new HashMap<>();
  
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException{
    byte[] byteCode = classesMap.get(name);
    if(byteCode != null){
      return defineClass(name, byteCode, 0, byteCode.length);
    }
    return super.findClass(name);
  }
  
  public void assignClasses(String name, byte[] classContent){
    classesMap.put(name, classContent);
  }
}
