package universecore.androidcore.classes;

import com.android.dex.Dex;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.merge.DexMerger;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import universecore.util.classes.AbstractGeneratedClassLoader;
import universecore.util.classes.JarList;
import universecore.util.handler.FieldHandler;
import universecore.util.handler.MethodHandler;
import universecore.util.mods.ModInfo;

import java.io.File;
import java.io.IOException;

public class AndroidGeneratedClassLoader extends AbstractGeneratedClassLoader {
  private ClassLoader dvLoader;
  protected final ModInfo mod;

  public AndroidGeneratedClassLoader(ModInfo mod, ClassLoader parent){
    super(JarList.inst().loadCacheFile(mod).file(), parent);
    this.mod = mod;

    updateLoader();
  }

  @Override
  public void declareClass(String name, byte[] byteCode){
    DxContext context = new DxContext();

    try{
      byte[] out;
      if(file.exists()){
        DexMerger merger = MethodHandler.newInstanceDefault(DexMerger.class,
            new Dex[]{new Dex(file), new Dex(byteCode)},
            context
        );
        out = merger.merge().getBytes();
      }
      else{
        out = byteCode;
      }
      DexLoaderFactory.writeFile(out, file);

      updateLoader();
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  private void updateLoader(){
    dvLoader = new DexClassLoader(file.getPath(), file.getParentFile().getPath() + "/oct", null, getParent());
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
          MethodHandler.invokeDefault(pathList, "addDexPath", file.getPath(), new File(file.getParentFile(), "/oct"));

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
