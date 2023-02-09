package universecore.androidcore.handler;

import dynamilize.DynamicMaker;
import dynamilize.DynamicObject;
import dynamilize.IllegalHandleException;
import dynamilize.JavaHandleHelper;
import dynamilize.classmaker.*;
import dynamilize.classmaker.code.IClass;
import dynamilize.classmaker.code.ILocal;
import mindustry.Vars;
import mindustry.mod.Mod;
import universecore.ImpCore;
import universecore.androidcore.classes.AndroidGeneratedClassLoader;
import universecore.androidcore.classes.DexGenerator;
import universecore.androidcore.classes.DexLoaderFactory;
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
import java.util.ArrayList;
import java.util.Arrays;

public class AndroidClassHandler implements ClassHandler{
  private static final BaseDynamicClassLoader dynamicLoader =
      DexLoaderFactory.getClassLoader(AndroidClassHandler.class.getClassLoader());

  private final BaseGeneratedClassLoader generatedLoader;

  private boolean generateFinished;

  private DexGenerator generator;
  private DynamicMaker maker;

  private final ModInfo mod;

  public AndroidClassHandler(ModInfo mod){
    this.mod = mod;
    this.generatedLoader = new AndroidGeneratedClassLoader(mod, Vars.mods.mainLoader());
  }

  @Override
  @SuppressWarnings("unchecked")
  public ClassHandler newInstance(Class<?> modMain){
    if(!Mod.class.isAssignableFrom(modMain))
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
      public void setAccessor(Class<?> accessor){
        currLoader().setAccessor(accessor);
      }

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

      @SuppressWarnings({"unchecked", "rawtypes"})
      @Override
      protected <T> ClassInfo<? extends T> makeClassInfo(Class<T> baseClass, Class<?>[] interfaces){
        ClassInfo<? extends T> classInfo = super.makeClassInfo(baseClass, interfaces);
          try{
            Class<?> staticImpl = DynamicObject.class.getClassLoader().loadClass(DynamicObject.class.getName() + "$-CC");
            ClassInfo<?> interType = ClassInfo.asType(staticImpl);

            for(Method method: DynamicObject.class.getMethods()){
              if((method.getModifiers() & Modifier.STATIC) != 0) continue;

              ArrayList<Class<?>> args = new ArrayList<>();
              args.add(DynamicObject.class);
              args.addAll(Arrays.asList(method.getParameterTypes()));

              ClassInfo<?> retType = ClassInfo.asType(method.getReturnType());
              MethodInfo<?, ?> implMethod;
              try{
                implMethod = interType.getMethod(
                    retType,
                    "$default$" + method.getName(),
                    args.stream().map(ClassInfo::asType).toArray(IClass[]::new)
                );
              }catch(IllegalHandleException ignored){
                continue;
              }

              CodeBlock<?> code = classInfo.declareMethod(
                  Modifier.PUBLIC,
                  method.getName(),
                  retType,
                  Parameter.trans(Arrays.stream(method.getParameterTypes()).map(ClassInfo::asType).toArray(IClass[]::new))
              );

              if(implMethod.returnType() == ClassInfo.VOID_TYPE){
                code.invoke(null, implMethod, null, code.getParamAll().toArray(new ILocal[0]));
              }
              else{
                ILocal ret = code.local(retType);
                code.invoke(null, implMethod, ret, code.getParamAll().toArray(new ILocal[0]));
                code.returnValue(ret);
              }
            }
          }catch(ClassNotFoundException ignored){}

        return classInfo;
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
