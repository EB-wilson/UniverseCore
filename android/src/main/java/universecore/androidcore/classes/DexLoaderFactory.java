package universecore.androidcore.classes;

import com.android.dex.Dex;
import com.android.dex.DexFormat;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.merge.DexMerger;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import universecore.util.classes.BaseDynamicClassLoader;
import universecore.util.classes.JarList;
import universecore.util.handler.FieldHandler;
import universecore.util.handler.MethodHandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class DexLoaderFactory{
  private static Class<?> inMemoryLoaderClass;
  private static Constructor<?> inMemLoaderCstr;

  static{
    try{
      inMemoryLoaderClass = Class.forName("dalvik.system.InMemoryDexClassLoader");
      inMemLoaderCstr = inMemoryLoaderClass.getConstructor(ByteBuffer.class, ClassLoader.class);
    }catch(ClassNotFoundException|NoSuchMethodException ignored){}
  }

  public static AsClassDexLoader getClassLoader(ClassLoader parent){
    if(inMemoryLoaderClass != null) return new MemoryDexClassLoader(parent);
    return new DexFileClassLoader(parent);
  }

  public static void writeFile(byte[] data, File file) throws IOException{
    if(!file.exists()) file.createNewFile();

    try(JarOutputStream out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(file)))){
      JarEntry entry = new JarEntry(DexFormat.DEX_IN_JAR_NAME);
      entry.setSize(data.length);
      out.putNextEntry(entry);
      out.write(data);
      out.finish();
      out.flush();
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  public static abstract class AsClassDexLoader extends BaseDynamicClassLoader{
    protected AsClassDexLoader(ClassLoader parent){
      super(parent);
    }

    public abstract void reset();
  }

  public static class DexFileClassLoader extends AsClassDexLoader{
    private ClassLoader dvLoader;

    private DexFileClassLoader(ClassLoader parent){
      super(parent);
      updateLoader();

      reset();
    }

    @Override
    public void reset(){
      file.delete();
    }

    @Override
    public void declareClass(String name, byte[] byteCode){
      try{
        byte[] bytes = MethodHandler.newInstanceDefault(DexMerger.class,
            new Dex[]{new Dex(file), new Dex(byteCode)},
            new DxContext()
        ).merge().getBytes();

        writeFile(bytes, file);
        updateLoader();
      }catch(IOException e){
        throw new RuntimeException(e);
      }
    }

    private void updateLoader(){
      dvLoader = new DexClassLoader(file.getPath(), JarList.jarFileCache.path() + "/oct", null, getParent());
    }

    @Override
    public Class<?> loadClass(String name, Class<?> accessor, boolean resolve) throws ClassNotFoundException{
      Class<?> c;

      if (accessor != null){
        if (accessor.getClassLoader() instanceof BaseDexClassLoader loader) {
          try {
            return loader.loadClass(name);
          } catch (ClassNotFoundException cf) {
            Object pathList = FieldHandler.getValueDefault(loader, "pathList");
            MethodHandler.invokeDefault(pathList, "addDexPath", file.getPath(), new File(JarList.jarFileCache.file(), "/oct"));

            return loader.loadClass(name);
          }
        }
        else throw new RuntimeException("unusable access " + accessor + " in loader: " + accessor.getClassLoader());
      }
      else c = dvLoader.loadClass(name);

      if(resolve) resolveClass(c);

      return c;
    }
  }

  public static class MemoryDexClassLoader extends AsClassDexLoader{
    private Dex dex;

    private ClassLoader dvLoader;

    private MemoryDexClassLoader(ClassLoader parent){
      super(parent);
    }


    @Override
    public void reset(){
      dex = null;
    }

    @Override
    public void declareClass(String name, byte[] byteCode){
      try{
        dex = MethodHandler.newInstanceDefault(DexMerger.class,
            new Dex[]{new Dex(byteCode), dex},
            new DxContext()
        ).merge();

        dvLoader = (ClassLoader) inMemLoaderCstr.newInstance(ByteBuffer.wrap(dex.getBytes()), getParent());
      }catch(InstantiationException|IllegalAccessException|InvocationTargetException|IOException e){
        throw new RuntimeException(e);
      }
    }

    @Override
    public Class<?> loadClass(String name, Class<?> accessor, boolean resolve) throws ClassNotFoundException{
      Class<?> c;

      if (accessor != null){
        if (accessor.getClassLoader() instanceof BaseDexClassLoader loader) {
          try {
            return loader.loadClass(name);
          } catch (ClassNotFoundException cf) {
            Object pathList = FieldHandler.getValueDefault(loader, "pathList");
            MethodHandler.invokeDefault(pathList, "initByteBufferDexPath", (Object) new ByteBuffer[]{ByteBuffer.wrap(dex.getBytes())});

            return loader.loadClass(name);
          }
        }
        else throw new RuntimeException("unusable access " + accessor + " in loader: " + accessor.getClassLoader());
      }
      else c = dvLoader.loadClass(name);

      if(resolve) resolveClass(c);

      return c;
    }
  }
}
