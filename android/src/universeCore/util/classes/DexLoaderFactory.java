package universeCore.util.classes;

import com.android.dex.Dex;
import com.android.dex.DexFormat;
import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.cf.direct.StdAttributeFactory;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.android.dx.dex.file.DexFile;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class DexLoaderFactory{
  public static AsClassDexLoader generateClassLoader(ClassLoader parent){
    try{
      return new MemoryDexClassLoader(Class.forName("dalvik.system.InMemoryDexClassLoader"), parent);
    }catch(ClassNotFoundException e){
      try{
        return new DexFileClassLoader(Class.forName("dalvik.system.DexClassLoader"), parent);
      }catch(ClassNotFoundException ignored){
        throw new RuntimeException("can not create a DexLoader on non-dalvik VM platform");
      }
    }
  }
  
  public static abstract class AsClassDexLoader extends BaseDynamicClassLoader{
    protected final Class<?> dvLoaderClass;
    protected DxContext context;
    
    protected AsClassDexLoader(Class<?> loaderClass, ClassLoader parent){
      super(parent);
      dvLoaderClass = loaderClass;
    }
    
    public void defineClass(String name, byte[] code){
      try{
        DexOptions options = new DexOptions();
        DexFile dexFile = new DexFile(options);
        DirectClassFile classFile = new DirectClassFile(code, name.replace(".", "/") + ".class", true);
        classFile.setAttributeFactory(StdAttributeFactory.THE_ONE);
        classFile.getMagic();
        context = new DxContext();
        dexFile.add(CfTranslator.translate(context, classFile, null, new CfOptions(), options, dexFile));
        Dex resultDex = new Dex(dexFile.toDex(null, false));
        writeFile(merge(resultDex.getBytes()));
        loadJar();
      }catch(IOException e){
        throw new RuntimeException(e);
      }
    }
    
    public abstract void reset();
  }
  
  public static class DexFileClassLoader extends AsClassDexLoader{
    private DexFileClassLoader(Class<?> loaderClass, ClassLoader parent) throws ClassNotFoundException{
      super(loaderClass, parent);
      reset();
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
  
    @Override
    protected ClassLoader getVMLoader(){
      try{
        return (ClassLoader)dvLoaderClass.getConstructor(String.class, String.class, String.class, ClassLoader.class)
            .newInstance(file.getPath(), file.getPath() + "/oct", null, getParent());
      }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
  
    @Override
    public byte[] merge(byte[] code){
      try{
        return new DexMerger(new Dex[]{new Dex(file), new Dex(code)}, CollisionPolicy.KEEP_FIRST, context).merge().getBytes();
      }catch(IOException e){
        throw new RuntimeException(e);
      }
    }
  
    @Override
    public void reset(){
      file.delete();
    }
  }
  
  public static class MemoryDexClassLoader extends AsClassDexLoader{
    private Dex dex;
  
    private MemoryDexClassLoader(Class<?> loaderClass, ClassLoader parent) throws ClassNotFoundException{
      super(loaderClass, parent);
    }
  
    @Override
    protected ClassLoader getVMLoader(){
      try{
        return (ClassLoader) dvLoaderClass.getConstructor(ByteBuffer.class, ClassLoader.class).newInstance(ByteBuffer.wrap(dex.getBytes()), getParent());
      }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
  
    public void writeFile(byte[] data){
      try{
        dex = new Dex(data);
      }catch(IOException e){
        throw new RuntimeException(e);
      }
    }
  
    @Override
    public boolean fileExist(){
      return dex != null;
    }
  
    @Override
    public byte[] merge(byte[] code){
      try{
        return new DexMerger(new Dex[]{dex, new Dex(code)}, CollisionPolicy.KEEP_FIRST, context).merge().getBytes();
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
