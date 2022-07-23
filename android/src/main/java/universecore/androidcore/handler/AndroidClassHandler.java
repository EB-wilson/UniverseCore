package universecore.androidcore.handler;

import dynamilize.DynamicMaker;
import dynamilize.classmaker.AbstractClassGenerator;
import dynamilize.classmaker.ByteClassLoader;
import dynamilize.classmaker.ClassInfo;
import mindustry.Vars;
import mindustry.mod.Mod;
import universecore.ImpCore;
import universecore.androidcore.classes.AndroidGeneratedClassLoader;
import universecore.androidcore.classes.DexGenerator;
import universecore.androidcore.classes.DexLoaderFactory;
import universecore.util.classes.AbstractFileClassLoader;
import universecore.util.classes.BaseDynamicClassLoader;
import universecore.util.classes.BaseGeneratedClassLoader;
import universecore.util.handler.ClassHandler;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

public class AndroidClassHandler implements ClassHandler{
  private static final BaseDynamicClassLoader dynamicLoader =
      DexLoaderFactory.getClassLoader(AndroidClassHandler.class.getClassLoader());

  private final BaseGeneratedClassLoader generatedLoader;

  private boolean generateFinished;

  private DexGenerator generator;
  private DynamicMaker maker;

  public AndroidClassHandler(ModInfo mod){
    this.generatedLoader = new AndroidGeneratedClassLoader(mod, Vars.mods.mainLoader());
  }

  @Override
  @SuppressWarnings("unchecked")
  public ClassHandler newInstance(Class<?> modMain){
    if(!modMain.isAssignableFrom(Mod.class))
      throw new IllegalArgumentException("require class is child of Mod");

    ModInfo mod = ModGetter.getModWithClass((Class<? extends Mod>) modMain);
    if(mod == null)
      throw new IllegalArgumentException("mod that inputted main class was not found");

    return new AndroidClassHandler(mod);
  }

  @Override
  public AbstractClassGenerator getGenerator(){
    return generator != null? generator: (generator = new DexGenerator(new ByteClassLoader(){
      @Override
      public void declareClass(String name, byte[] byteCode){
        currLoader().declareClass(name, byteCode);
      }

      @Override
      public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
        return currLoader().loadClass(name, resolve);
      }
    }){
      @Override
      @SuppressWarnings("unchecked")
      protected <T> Class<T> generateClass(ClassInfo<T> classInfo){
        try{
          return (Class<T>) currLoader().loadClass(classInfo.name());
        }catch(ClassNotFoundException ignored){
          return super.generateClass(classInfo);
        }
      }
    });
  }

  @Override
  public DynamicMaker getDynamicMaker(){
    return maker != null? maker: (maker = new DynamicMaker(accessibleObject -> {
      ImpCore.accessAndModifyHelper.setAccessible(accessibleObject);
    }){
      @Override
      @SuppressWarnings("unchecked")
      protected <T> Class<? extends T> generateClass(Class<T> baseClass, Class<?>[] interfaces){
        String name = getDynamicName(baseClass, interfaces);

        try{
          return (Class<? extends T>) currLoader().loadClass(name);
        }catch(ClassNotFoundException ignored){
          return makeClassInfo(baseClass, interfaces).generate(getGenerator());
        }
      }
    });
  }

  @Override
  public AbstractFileClassLoader currLoader(){
    return generateFinished? dynamicLoader: generatedLoader;
  }

  @Override
  public void finishGenerate(){
    generateFinished = true;
  }
}
