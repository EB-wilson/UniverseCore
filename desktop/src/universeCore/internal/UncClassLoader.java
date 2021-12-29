package universeCore.internal;

import arc.util.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UncClassLoader extends ClassLoader{
  protected final HashMap<String, JavaStringObject> filesMap = new HashMap<>();
  
  public UncClassLoader(ClassLoader parent){
    super(parent);
  }
  
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException{
    JavaStringObject fileObject;
    if((fileObject = filesMap.get(name)) != null){
      byte[] byteCode = fileObject.toByteCode();
      return defineClass(name, byteCode, 0, byteCode.length);
    }
    return super.findClass(name);
  }
  
  @Nullable
  @Override
  public InputStream getResourceAsStream(String name) {
    if (name.endsWith(".class")) {
      String qualifiedClassName = name.substring(0, name.length() - 6).replace('/', '.');
      JavaStringObject javaFileObject = filesMap.get(qualifiedClassName);
      if (null != javaFileObject && null != javaFileObject.toByteCode()) {
        return new ByteArrayInputStream(javaFileObject.toByteCode());
      }
    }
    return super.getResourceAsStream(name);
  }
  
  public void addObject(String className, JavaStringObject object){
    filesMap.put(className, object);
  }
  
  public List<JavaStringObject> fileList(){
    return Arrays.asList(filesMap.values().toArray(JavaStringObject[]::new));
  }
}
