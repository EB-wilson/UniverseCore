package universecore.desktopcore.handler;

import dynamilize.DynamicMaker;
import dynamilize.JavaHandleHelper;
import dynamilize.classmaker.ASMGenerator;
import dynamilize.classmaker.AbstractClassGenerator;
import dynamilize.classmaker.ByteClassLoader;
import dynamilize.classmaker.ClassInfo;
import mindustry.Vars;
import mindustry.mod.Mod;
import org.objectweb.asm.Opcodes;
import universecore.ImpCore;
import universecore.desktopcore.classes.DesktopDynamicClassLoader;
import universecore.desktopcore.classes.DesktopGeneratedClassLoader;
import universecore.util.classes.AbstractFileClassLoader;
import universecore.util.classes.BaseDynamicClassLoader;
import universecore.util.classes.BaseGeneratedClassLoader;
import universecore.util.classes.JarList;
import universecore.util.handler.ClassHandler;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DesktopClassHandler implements ClassHandler{
  private final BaseDynamicClassLoader dynamicLoader;
  private final BaseGeneratedClassLoader generatedLoader;

  private boolean generateFinished;

  private ASMGenerator generator;
  private DynamicMaker maker;

  private final ModInfo mod;

  public DesktopClassHandler(ModInfo mod){
    this.mod = mod;
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

  @Override
  public DynamicMaker getDynamicMaker(){
    return maker != null? maker: (maker = new DynamicMaker(new JavaHandleHelper() {
      @Override
      public <T> T newInstance(Constructor<? extends T> cstr, Object... args) {
        return ImpCore.methodInvokeHelper.newInstance(cstr.getDeclaringClass(), args);
      }

      @Override
      public <R> R invoke(Method method, Object target, Object... args) {
        if (Modifier.isStatic(method.getModifiers())) {
          return ImpCore.methodInvokeHelper.invokeStatic(method.getDeclaringClass(), method.getName(), args);
        }
        return ImpCore.methodInvokeHelper.invoke(target, method.getName(), args);
      }

      @Override
      public <T> T get(Field field, Object target) {
        if (Modifier.isStatic(field.getModifiers())) {
          return ImpCore.fieldAccessHelper.getStatic(field.getDeclaringClass(), field.getName());
        }
        else return ImpCore.fieldAccessHelper.get(target, field.getName());
      }

      @Override
      public void set(Field field, Object target, Object value) {
        if (Modifier.isStatic(field.getModifiers())) {
          ImpCore.fieldAccessHelper.setStatic(field.getDeclaringClass(), field.getName(), value);
        }
        else ImpCore.fieldAccessHelper.set(target, field.getName(), value);
      }
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
    JarList.inst().update(mod);
  }
}
