package universecore.desktopcore.handler;

import dynamilize.DynamicMaker;
import dynamilize.PackageAccHandler;
import dynamilize.classmaker.ASMGenerator;
import dynamilize.classmaker.AbstractClassGenerator;
import dynamilize.classmaker.ByteClassLoader;
import dynamilize.classmaker.ClassInfo;
import dynamilize.unc.UncDefaultHandleHelper;
import mindustry.Vars;
import mindustry.mod.Mod;
import org.objectweb.asm.Opcodes;
import universecore.desktopcore.classes.DesktopDynamicClassLoader;
import universecore.desktopcore.classes.DesktopGeneratedClassLoader;
import universecore.util.classes.AbstractFileClassLoader;
import universecore.util.classes.BaseDynamicClassLoader;
import universecore.util.classes.BaseGeneratedClassLoader;
import universecore.util.classes.JarList;
import universecore.util.handler.ClassHandler;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

@SuppressWarnings("DuplicatedCode")
public class DesktopClassHandler implements ClassHandler{
  protected BaseDynamicClassLoader dynamicLoader;
  protected BaseGeneratedClassLoader generatedLoader;

  private boolean generateFinished;

  protected ASMGenerator generator;
  protected PackageAccHandler accHandler;
  protected DynamicMaker maker;

  protected final ModInfo mod;

  public DesktopClassHandler(ModInfo mod){
    this.mod = mod;
    initLoaders();
  }

  protected void initLoaders(){
    this.generatedLoader = new DesktopGeneratedClassLoader(mod, Vars.mods.mainLoader());
    this.dynamicLoader = new DesktopDynamicClassLoader(this.generatedLoader);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ClassHandler newInstance(Class<?> modMain){
    if(!Mod.class.isAssignableFrom(modMain))
      throw new IllegalArgumentException("require class is child of Mod");

    ModInfo mod = ModGetter.getModWithClass((Class<? extends Mod>) modMain);
    if(mod == null)
      throw new IllegalArgumentException("mod that inputted main class was not found");

    return new DesktopClassHandler(mod);
  }

  @Override
  public AbstractClassGenerator getGenerator(){
    return generator != null? generator: (generator = new ASMGenerator(new ByteClassLoader(){
      @Override
      public void declareClass(String name, byte[] byteCode){
        currLoader().declareClass(name, byteCode);
      }

      @Override
      public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
        return currLoader().loadClass(name, resolve);
      }
    }, Opcodes.V1_8){
      @Override
      @SuppressWarnings("unchecked")
      protected <T> Class<T> generateClass(ClassInfo<T> classInfo) throws ClassNotFoundException{
        try{
          return (Class<T>) currLoader().loadClass(classInfo.name());
        }catch(ClassNotFoundException ignored){
          return super.generateClass(classInfo);
        }
      }
    });
  }

  public PackageAccHandler getAccHandler(){
    return accHandler != null? accHandler: (accHandler = new PackageAccHandler() {
      @SuppressWarnings("unchecked")
      @Override
      protected <T> Class<? extends T> loadClass(ClassInfo<?> clazz, Class<T> baseClass) {
        try {
          return (Class<? extends T>) new ASMGenerator(new ByteClassLoader() {
            @Override
            public void declareClass(String s, byte[] bytes) {
              currLoader().declareClass(s, bytes);
            }

            @Override
            public Class<?> loadClass(String s, boolean b) throws ClassNotFoundException {
              return currLoader().loadClass(s, baseClass, b);
            }
          }, Opcodes.V1_8){
            @Override
            protected <Ty> Class<Ty> generateClass(ClassInfo<Ty> classInfo) throws ClassNotFoundException {
              try{
                return (Class<Ty>) currLoader().loadClass(classInfo.name(), baseClass, false);
              }catch(ClassNotFoundException ignored){
                return super.generateClass(classInfo);
              }
            }
          }.generateClass(clazz);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  @Override
  public DynamicMaker getDynamicMaker(){
    return maker != null? maker: (maker = new DynamicMaker(new UncDefaultHandleHelper()){
      @Override
      @SuppressWarnings("unchecked")
      protected <T> Class<? extends T> generateClass(Class<T> baseClass, Class<?>[] interfaces, Class<?>[] aspects){
        String name = getDynamicName(baseClass, interfaces);

        try{
          return (Class<? extends T>) currLoader().loadClass(name);
        }catch(ClassNotFoundException ignored){
          return makeClassInfo(baseClass, interfaces, aspects).generate(getGenerator());
        }
      }

      @Override
      protected <T> Class<? extends T> handleBaseClass(Class<T> baseClass) {
        return getAccHandler().handle(baseClass);
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
    JarList.inst().update(mod);
  }
}
