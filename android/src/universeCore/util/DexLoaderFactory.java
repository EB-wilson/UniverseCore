package universeCore.util;

import arc.Core;
import arc.files.Fi;
import com.android.dex.Dex;
import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.cf.direct.StdAttributeFactory;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.android.dx.dex.file.DexFile;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class DexLoaderFactory{
  public static final Fi dexFileCache = Core.settings.getDataDirectory().child("mods").child("data").child("cache");
  
  public static AsClassDexLoader generateClassLoader(){
    return generateClassLoader(dexFileCache.file());
  }
  
  public static AsClassDexLoader generateClassLoader(File file){
    try{
      return new MemoryDexClassLoader(Class.forName("dalvik.system.InMemoryDexClassLoader"));
    }catch(ClassNotFoundException e){
      try{
        return new DexFileClassLoader(Class.forName("dalvik.system.DexClassLoader"), file);
      }catch(ClassNotFoundException ignored){
        throw new RuntimeException("can not create a DexLoader on non-dalvik VM platform");
      }
    }
  }
  
  public static abstract class AsClassDexLoader extends ClassLoader{
    protected final Class<?> dvLoaderClass;
    
    protected AsClassDexLoader(Class<?> loaderClass){
      dvLoaderClass = loaderClass;
    }
    
    public Class<?> defineClass(String name, byte[] code){
      try{
        DexOptions options = new DexOptions();
        DexFile dexFile = new DexFile(options);
        DirectClassFile classFile = new DirectClassFile(code, name.replace(".", "/") + ".class", true);
        classFile.setAttributeFactory(StdAttributeFactory.THE_ONE);
        classFile.getMagic();
        DxContext context = new DxContext();
        dexFile.add(CfTranslator.translate(context, classFile, null, new CfOptions(), options, dexFile));
        Dex resultDex = new Dex(dexFile.toDex(null, false));
        merge(resultDex, context);
        return load(name);
      }catch(IOException | ClassNotFoundException e){
        throw new RuntimeException(e);
      }
    }
  
    protected abstract ClassLoader getDvLoader();
  
    public Class<?> load(String name) throws ClassNotFoundException{
      return getDvLoader().loadClass(name);
    }
    
    public abstract void merge(Dex newerDex, DxContext context);
    
    public abstract void reset();
  }
  
  public static class DexFileClassLoader extends AsClassDexLoader{
    private static int fileCount;
    private final File dexFile;
    
    private DexFileClassLoader(Class<?> loaderClass, File cacheDir){
      super(loaderClass);
      dexFile = new File(cacheDir, (fileCount++) + ".dex");
      cacheDir.mkdirs();
      reset();
    }
  
    @Override
    protected ClassLoader getDvLoader(){
      try{
        return (ClassLoader)dvLoaderClass.getConstructor(String.class, String.class, String.class, ClassLoader.class)
            .newInstance(dexFile.getPath(), dexFileCache.child("oct").path(), null, getParent());
      }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
  
    @Override
    public void merge(Dex newerDex, DxContext context){
      try{
        new DexMerger(new Dex[]{new Dex(dexFile), newerDex}, CollisionPolicy.KEEP_FIRST, context).merge().writeTo(dexFile);
      }catch(IOException e){
        throw new RuntimeException(e);
      }
    }
  
    @Override
    public void reset(){
      dexFile.delete();
    }
  }
  
  public static class MemoryDexClassLoader extends AsClassDexLoader{
    private Dex dex;
  
    private MemoryDexClassLoader(Class<?> loaderClass){
      super(loaderClass);
    }
  
    @Override
    protected ClassLoader getDvLoader(){
      try{
        return (ClassLoader) dvLoaderClass.getConstructor(ByteBuffer.class, ClassLoader.class).newInstance(ByteBuffer.wrap(dex.getBytes()), getParent());
      }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
  
    @Override
    public void merge(Dex newerDex, DxContext context){
      try{
        dex = new DexMerger(new Dex[]{dex, newerDex}, CollisionPolicy.KEEP_FIRST, context).merge();
      }catch(IOException e){
        throw new RuntimeException(e);
      }
    }
  
    @Override
    public void reset(){
      dex = null;
    }
  }
}
