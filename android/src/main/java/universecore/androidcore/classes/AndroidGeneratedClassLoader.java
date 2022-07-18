package universecore.androidcore.classes;

import com.android.dex.Dex;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import universecore.util.classes.BaseGeneratedClassLoader;
import universecore.util.mods.ModInfo;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("unchecked")
public class AndroidGeneratedClassLoader extends BaseGeneratedClassLoader{
  protected static final Class<? extends ClassLoader> dvLoaderClass;

  protected static final Constructor<? extends ClassLoader> loaderCstr;

  static{
    try{
      dvLoaderClass = (Class<? extends ClassLoader>) Class.forName("dalvik.system.DexClassLoader");
      loaderCstr = dvLoaderClass.getConstructor(String.class, String.class, String.class, ClassLoader.class);
    }catch(ClassNotFoundException|NoSuchMethodException e){
      throw new RuntimeException(e);
    }
  }

  private final ClassLoader dvLoader;

  public AndroidGeneratedClassLoader(ModInfo mod, ClassLoader parent){
    super(mod, parent);
    try{
      dvLoader = loaderCstr.newInstance(file.getPath(), file.getParentFile().getPath() + "/oct", null, parent);
    }catch(InstantiationException|InvocationTargetException|IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void declareClass(String name, byte[] byteCode){
    DxContext context = new DxContext();

    try{
      DexMerger merger = new DexMerger(
          new Dex[]{new Dex(file), new Dex(byteCode)},
          CollisionPolicy.KEEP_FIRST,
          context
      );
      byte[] out = merger.merge().getBytes();
      DexLoaderFactory.writeFile(out, file);
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
    Class<?> c = dvLoader.loadClass(name);
    if(resolve) resolveClass(c);

    return c;
  }
}
