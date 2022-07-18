package universecore.desktopcore.handler;

import dynamilize.DynamicMaker;
import dynamilize.classmaker.ASMGenerator;
import dynamilize.classmaker.AbstractClassGenerator;
import dynamilize.classmaker.ByteClassLoader;
import dynamilize.classmaker.ClassInfo;
import mindustry.Vars;
import org.objectweb.asm.Opcodes;
import universecore.ImpCore;
import universecore.desktopcore.classes.DesktopDynamicClassLoader;
import universecore.desktopcore.classes.DesktopGeneratedClassLoader;
import universecore.util.classes.AbstractFileClassLoader;
import universecore.util.classes.BaseDynamicClassLoader;
import universecore.util.classes.BaseGeneratedClassLoader;
import universecore.util.handler.ClassHandler;
import universecore.util.mods.ModInfo;

public class DesktopClassHandler implements ClassHandler{
  private final BaseDynamicClassLoader dynamicLoader;
  private final BaseGeneratedClassLoader generatedLoader;

  private boolean generateFinished;

  private ASMGenerator generator;
  private DynamicMaker maker;

  public DesktopClassHandler(ModInfo mod){
    this.generatedLoader = new DesktopGeneratedClassLoader(mod, Vars.mods.mainLoader());
    this.dynamicLoader = new DesktopDynamicClassLoader(this.generatedLoader);
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
    }, Opcodes.V11){
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
