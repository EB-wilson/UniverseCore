package universecore.desktopcore.classes;

import arc.Core;
import arc.files.Fi;
import universecore.util.mods.ModInfo;
import universecore.util.classes.BaseGeneratedClassLoader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class DesktopGeneratedClassLoader extends BaseGeneratedClassLoader{
  private static final Method addURL;
  private static final Fi jarFileCache = Core.settings.getDataDirectory().child("universecore").child("cache");
  
  static{
    try{
      addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      addURL.setAccessible(true);
    }catch(NoSuchMethodException e){
      throw new RuntimeException(e);
    }
  }

  public DesktopGeneratedClassLoader(ModInfo mod, File cacheFile, ClassLoader parent){
    super(mod, cacheFile, parent);
  }

  @Override
  public ClassLoader getVMLoader(){
    try{
      return new URLClassLoader(new URL[]{file.toURI().toURL()});
    }catch(MalformedURLException e){
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public byte[] merge(byte[] other){
    try{
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      JarOutputStream jarOut = new JarOutputStream(byteOut);
      
      String tempName = String.valueOf(System.nanoTime());
      String s = tempName.substring(tempName.length() - 6);
      File temp = jarFileCache.child("temp" + s).file();
      FileOutputStream out = new FileOutputStream(temp);
      out.write(other);
      out.close();
      for(File f : new File[]{file, temp}){
        JarFile jf = new JarFile(f);
        JarEntry entry;
        Enumeration<? extends JarEntry> enumeration = jf.entries();
        while(enumeration.hasMoreElements()){
          entry = enumeration.nextElement();
          if(!entry.isDirectory()){
            JarEntry outEntry = new JarEntry(entry.getName());
            jarOut.putNextEntry(outEntry);
            if(entry.getSize() > 0){
              DataInputStream input = new DataInputStream(jf.getInputStream(entry));
              byte[] cache = new byte[1024];
              int length;
              while((length = input.read(cache)) != -1){
                jarOut.write(cache, 0, length);
              }
              input.close();
            }
          }
        }
      }
      byte[] result = byteOut.toByteArray();
      jarOut.close();
      byteOut.close();
      temp.delete();
      return result;
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public Class<?> loadClass(String name, Class<?> neighbor) throws ClassNotFoundException{
    ClassLoader l = neighbor.getClassLoader();
    if(l instanceof URLClassLoader){
      try{
        ClassLoader lo = new ClassLoader(){
          @Override
          protected Class<?> findClass(String name) throws ClassNotFoundException{
            try{
              return l.loadClass(name);
            }catch(ClassNotFoundException e){
              return DesktopGeneratedClassLoader.this.loadClass(name);
            }
          }
        };
        addURL.invoke(l, file.toURI().toURL());
        return lo.loadClass(name);
      }catch(IllegalAccessException | InvocationTargetException | MalformedURLException e){
        throw new RuntimeException(e);
      }
    }
    throw new RuntimeException();
  }
  
  @Override
  public void writeFile(byte[] data){
    try{
      new FileOutputStream(file, false).write(data);
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
}
