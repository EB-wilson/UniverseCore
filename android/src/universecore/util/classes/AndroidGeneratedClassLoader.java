package universecore.util.classes;

import com.android.dex.Dex;
import com.android.dex.DexFormat;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

@SuppressWarnings("unchecked")
public class AndroidGeneratedClassLoader extends BaseGeneratedClassLoader{
  protected Class<? extends ClassLoader> dvLoaderClass = (Class<? extends ClassLoader>) Class.forName("dalvik.system.DexClassLoader");
  protected Class<? extends ClassLoader> baseDexClassLoaderClass = (Class<? extends ClassLoader>) Class.forName("dalvik.system.BaseDexClassLoader");
  protected Method addDexPath = baseDexClassLoaderClass.getMethod("addDexPath", String.class);
  protected Constructor<? extends ClassLoader> loaderCstr = dvLoaderClass.getConstructor(String.class, String.class, String.class, ClassLoader.class);
  
  public AndroidGeneratedClassLoader(JarList.ModInfo mod, File cacheFile, ClassLoader parent) throws ClassNotFoundException, NoSuchMethodException{
    super(mod, cacheFile, parent);
  }
  
  @Override
  public ClassLoader getVMLoader(){
    try{
      return loaderCstr.newInstance(file.getPath(), JarList.jarFileCache.child("oct").path(), null, getParent());
    }catch(InstantiationException | IllegalAccessException | InvocationTargetException e){
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public byte[] merge(byte[] other){
    try{
      DxContext context = new DxContext();
      Dex old = new Dex(file), generate = new Dex(other);
      return new DexMerger(new Dex[]{old, generate}, CollisionPolicy.KEEP_FIRST, context).merge().getBytes();
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public Class<?> loadClass(String name, Class<?> neighbor) throws ClassNotFoundException{
    ClassLoader l = neighbor.getClassLoader();
    if(baseDexClassLoaderClass.isAssignableFrom(l.getClass())){
      ClassLoader lo = new ClassLoader(){
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException{
          try{
            return l.loadClass(name);
          }catch(ClassNotFoundException e){
            return AndroidGeneratedClassLoader.this.loadClass(name);
          }
        }
      };
      
      try{
        addDexPath.invoke(l, file.getPath());
        return lo.loadClass(name);
      }catch(IllegalAccessException | InvocationTargetException e){
        throw new RuntimeException(e);
      }
    }
    throw new RuntimeException();
  }
  
  public void writeFile(byte[] data){
    try(JarOutputStream out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
      JarEntry entry = new JarEntry(DexFormat.DEX_IN_JAR_NAME);
      entry.setSize(data.length);
      out.putNextEntry(entry);
      try{
        out.write(data);
      }finally{
        out.closeEntry();
      }
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
}
