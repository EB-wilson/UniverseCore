package universecore.androidcore.handler;

import dynamilize.DynamicMaker;
import dynamilize.DynamicObject;
import dynamilize.IllegalHandleException;
import dynamilize.PackageAccHandler;
import dynamilize.classmaker.*;
import dynamilize.classmaker.code.IClass;
import dynamilize.classmaker.code.ILocal;
import dynamilize.unc.UncDefaultHandleHelper;
import mindustry.Vars;
import mindustry.mod.Mod;
import universecore.androidcore.classes.AndroidGeneratedClassLoader;
import universecore.androidcore.classes.DexGenerator;
import universecore.androidcore.classes.DexLoaderFactory;
import universecore.util.classes.*;
import universecore.util.handler.ClassHandler;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

public class AndroidClassHandler implements ClassHandler{
  protected static final AbstractDynamicClassLoader dynamicLoader =
      DexLoaderFactory.getClassLoader(AndroidClassHandler.class.getClassLoader());

  protected final AbstractGeneratedClassLoader generatedLoader;

  protected boolean generateFinished;

  protected DexGenerator generator;
  protected PackageAccHandler accHandler;
  protected DynamicMaker maker;

  protected final ModInfo mod;

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

  public PackageAccHandler getAccHandler(){
    return accHandler != null? accHandler: (accHandler = new PackageAccHandler() {
      @SuppressWarnings("unchecked")
      @Override
      protected <T> Class<? extends T> loadClass(ClassInfo<?> clazz, Class<T> baseClass) {
        try {
          return (Class<? extends T>) new DexGenerator(new ByteClassLoader() {
            @Override
            public void declareClass(String s, byte[] bytes) {
              currLoader().declareClass(s, bytes);
            }

            @Override
            public Class<?> loadClass(String s, boolean b) throws ClassNotFoundException {
              return currLoader().loadClass(s, baseClass, b);
            }
          }){
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

      @SuppressWarnings({"unchecked", "rawtypes"})
      @Override
      protected <T> ClassInfo<? extends T> makeClassInfo(Class<T> baseClass, Class<?>[] interfaces, Class<?>[] aspects){
        ClassInfo<? extends T> classInfo = super.makeClassInfo(baseClass, interfaces,aspects);
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
