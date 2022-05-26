package universecore.desktopcore.classes;

import arc.Core;
import arc.files.Fi;
import universecore.util.classes.BaseDynamicClassLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class DesktopDynamicClassLoader extends BaseDynamicClassLoader{
  private final HashMap<String, byte[]> classes = new HashMap<>();
  
  public DesktopDynamicClassLoader(ClassLoader parent){
    super(parent);
    reset();
  }
  
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException{
    byte[] code = classes.get(name);
    if(code != null){
      return defineClass(name, code, 0, code.length);
    }
    return super.findClass(name);
  }
  
  @Override
  public byte[] merge(byte[] other){
    try{
      Fi temp = Core.files.cache("tempGenerate.jar");
      temp.writeBytes(other, false);
      JarFile tempJar = new JarFile(temp.file());
      JarInputStream input = new JarInputStream(new ByteArrayInputStream(other));
      JarEntry entry;
      InputStream dataIn;
      while((entry = input.getNextJarEntry()) != null){
        if(entry.isDirectory()) continue;
        dataIn = tempJar.getInputStream(entry);
        classes.put(entry.getName().replace("/", ".").replace(".class", ""), dataIn.readAllBytes());
      }
      temp.delete();
      
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      JarOutputStream out = new JarOutputStream(result);
      for(Map.Entry<String, byte[]> e : classes.entrySet()){
        out.putNextEntry(new JarEntry(e.getKey().replace("/", ".").replace(".class", "")));
        out.write(e.getValue());
      }
      return result.toByteArray();
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void writeFile(byte[] data){
    try{
      Fi temp = Core.files.cache("tempGenerate.jar");
      temp.writeBytes(data, false);
      JarFile tempJar = new JarFile(temp.file());
      JarInputStream input = new JarInputStream(new ByteArrayInputStream(data));
      JarEntry entry;
      InputStream dataIn;
      while((entry = input.getNextJarEntry()) != null){
        if(entry.isDirectory()) continue;
        dataIn = tempJar.getInputStream(entry);
        classes.put(entry.getName().replace("/", ".").replace(".class", ""), dataIn.readAllBytes());
      }
      temp.delete();
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void defineClass(String name, byte[] code){
    classes.put(name, code);
  }
  
  @Override
  protected ClassLoader getVMLoader(){
    return this;
  }
  
  @Override
  public void reset(){
    classes.clear();
  }
}
