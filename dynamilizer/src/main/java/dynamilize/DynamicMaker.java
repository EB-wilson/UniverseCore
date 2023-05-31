package dynamilize;

import dynamilize.classmaker.*;
import dynamilize.classmaker.code.*;
import dynamilize.classmaker.code.annotation.AnnotationType;
import dynamilize.unc.UncDefaultHandleHelper;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static dynamilize.classmaker.ClassInfo.*;

/**动态类型运作的核心工厂类型，用于将传入的动态类型与委托基类等构造出动态委托类型以及其实例。
 * <p>定义了基于{@link ASMGenerator}的默认生成器实现，通过{@link DynamicMaker#getDefault()}获取该实例以使用
 * <p>若需要特殊的实现，则需要重写/实现此类的方法，主要的方法：
 * <ul>
 * <li><strong>{@link DynamicMaker#makeClassInfo(Class, Class[])}</strong>：决定此工厂如何构造委托类的描述信息
 * <li><strong>{@link DynamicMaker#generateClass(Class, Class[])}</strong>：决定如何解析描述信息生成字节码以及如何加载字节码为java类
 * </ul>
 * 实施时必须按照方法的描述给出基本的实现，不应该改变原定的最低上下文请求。
 * <pre>
 * 使用方法可参阅：
 *   {@link DynamicClass}
 * DynamicMaker的方法：
 *   {@link DynamicMaker#newInstance(DynamicClass)}
 *   {@link DynamicMaker#newInstance(Class[], DynamicClass)}
 *   {@link DynamicMaker#newInstance(Class, DynamicClass, Object...)}
 *   {@link DynamicMaker#newInstance(Class, Class[], DynamicClass, Object...)}
 * </pre>
 *
 * @see DynamicClass
 * @see DynamicObject
 * @see DataPool
 *
 * @author EBwilson */
@SuppressWarnings({"rawtypes", "DuplicatedCode"})
public abstract class DynamicMaker{
  public static final String CALLSUPER = "$super";

  private static final HashSet<String> INTERNAL_FIELD = new HashSet<>(Arrays.asList(
      "$dynamic_type$",
      "$datapool$",
      "$varValuePool$",
      "$superbasepointer$"
  ));

  public static final ClassInfo<DynamicClass> DYNAMIC_CLASS_TYPE = asType(DynamicClass.class);
  public static final ClassInfo<DynamicObject> DYNAMIC_OBJECT_TYPE = asType(DynamicObject.class);
  public static final ClassInfo<DataPool> DATA_POOL_TYPE = asType(DataPool.class);
  public static final ClassInfo<FunctionType> FUNCTION_TYPE_TYPE = asType(FunctionType.class);
  public static final MethodInfo<FunctionType, FunctionType> TYPE_INST = FUNCTION_TYPE_TYPE.getMethod(
      FUNCTION_TYPE_TYPE,
      "inst",
      CLASS_TYPE.asArray());
  public static final ClassInfo<Function> FUNCTION_TYPE = asType(Function.class);
  public static final ClassInfo<DataPool.ReadOnlyPool> READONLY_POOL_TYPE = asType(DataPool.ReadOnlyPool.class);
  public static final ClassInfo<Integer> INTEGER_CLASS_TYPE = asType(Integer.class);
  public static final ClassInfo<HashMap> HASH_MAP_TYPE = asType(HashMap.class);
  public static final ClassInfo<IVariable> VAR_TYPE = ClassInfo.asType(IVariable.class);
  public static final ClassInfo<IllegalStateException> STATE_EXCEPTION_TYPE = asType(IllegalStateException.class);
  public static final ClassInfo<ArgumentList> ARG_LIST_TYPE = asType(ArgumentList.class);
  public static final ClassInfo<Function.SuperGetFunction> SUPER_GET_FUNC_TYPE = ClassInfo.asType(Function.SuperGetFunction.class);
  public static final ClassInfo<IFunctionEntry> FUNC_ENTRY_TYPE = ClassInfo.asType(IFunctionEntry.class);

  public static final IMethod<DataPool, DataPool.ReadOnlyPool> GET_READER = DATA_POOL_TYPE.getMethod(READONLY_POOL_TYPE, "getReader", DYNAMIC_OBJECT_TYPE);
  public static final IMethod<HashMap, Object> MAP_GET = HASH_MAP_TYPE.getMethod(OBJECT_TYPE, "get", OBJECT_TYPE);
  public static final IMethod<Integer, Integer> VALUE_OF = INTEGER_CLASS_TYPE.getMethod(INTEGER_CLASS_TYPE, "valueOf", INT_TYPE);
  public static final IMethod<HashMap, Object> MAP_PUT = HASH_MAP_TYPE.getMethod(OBJECT_TYPE, "put", OBJECT_TYPE, OBJECT_TYPE);
  public static final IMethod<DataPool, IVariable> GET_VAR = DATA_POOL_TYPE.getMethod(VAR_TYPE, "getVariable", STRING_TYPE);
  public static final IMethod<DataPool, Void> SET_VAR = DATA_POOL_TYPE.getMethod(ClassInfo.VOID_TYPE, "setVariable", VAR_TYPE);
  public static final IMethod<DataPool, Void> SETFUNC = DATA_POOL_TYPE.getMethod(ClassInfo.VOID_TYPE, "setFunction", STRING_TYPE, FUNCTION_TYPE, ClassInfo.CLASS_TYPE.asArray());
  public static final IMethod<DataPool, Void> SETFUNC2 = DATA_POOL_TYPE.getMethod(ClassInfo.VOID_TYPE, "setFunction", STRING_TYPE, SUPER_GET_FUNC_TYPE, ClassInfo.CLASS_TYPE.asArray());
  public static final IMethod<DataPool, IFunctionEntry> SELECT = DATA_POOL_TYPE.getMethod(FUNC_ENTRY_TYPE, "select", STRING_TYPE, FUNCTION_TYPE_TYPE);
  public static final IMethod<DataPool, Void> INIT = DATA_POOL_TYPE.getMethod(VOID_TYPE, "init", DYNAMIC_OBJECT_TYPE, OBJECT_TYPE.asArray());
  public static final IMethod<DynamicObject, Object> INVOKE = DYNAMIC_OBJECT_TYPE.getMethod(OBJECT_TYPE, "invokeFunc", FUNCTION_TYPE_TYPE, STRING_TYPE, OBJECT_TYPE.asArray());
  public static final IMethod<ArgumentList, Object[]> GET_LIST = ARG_LIST_TYPE.getMethod(OBJECT_TYPE.asArray(), "getList", INT_TYPE);
  public static final IMethod<ArgumentList, Void> RECYCLE_LIST = ARG_LIST_TYPE.getMethod(VOID_TYPE, "recycleList", OBJECT_TYPE.asArray());

  private static final MethodHandles.Lookup LOOKUP_INST = MethodHandles.lookup();
  private static final Map<String, Set<FunctionType>> OVERRIDES = new HashMap<>();
  private static final Set<Class<?>> INTERFACE_TEMP = new HashSet<>();
  private static final Class[] EMPTY_CLASSES = new Class[0];
  public static final ILocal[] LOCALS_EMP = new ILocal[0];
  public static final HashSet<FunctionType> EMP_MAP = new HashSet<>();
  private final JavaHandleHelper helper;

  private final HashMap<ClassImplements<?>, Class<?>> classPool = new HashMap<>();
  private final HashMap<Class<?>, DataPool> classPoolsMap = new HashMap<>();
  private final HashMap<Class<?>, HashMap<FunctionType, MethodHandle>> constructors = new HashMap<>();

  /**创建一个实例，并传入其要使用的{@linkplain JavaHandleHelper java行为支持器}，子类引用此构造器可能直接设置默认的行为支持器而无需外部传入*/
  protected DynamicMaker(JavaHandleHelper helper){
    this.helper = helper;
  }

  /**获取默认的动态类型工厂，工厂具备基于{@link ASMGenerator}与适用于<i>HotSpot JVM</i>运行时的{@link JavaHandleHelper}进行的实现。
   * 适用于：
   * <ul>
   * <li>java运行时版本1.8的所有jvm
   * <li>java运行时版本大于等于1.9的<i>甲骨文HotSpot JVM</i>
   * <li><strong><i>IBM OpenJ9</i>运行时尚未支持</strong>
   * </ul>
   * 若有范围外的需求，可按需要进行实现*/
  public static DynamicMaker getDefault(){
    BaseClassLoader loader = new BaseClassLoader(DynamicMaker.class.getClassLoader());
    ASMGenerator generator = new ASMGenerator(loader, Opcodes.V1_8);

    return new DynamicMaker(new UncDefaultHandleHelper()){
      @Override
      protected <T> Class<? extends T> generateClass(Class<T> baseClass, Class<?>[] interfaces){
        return makeClassInfo(baseClass, interfaces).generate(generator);
      }
    };
  }

  /**使用默认构造函数构造没有实现额外接口的动态类的实例，实例的java类型委托类为{@link Object}
   *
   * @param dynamicClass 用于实例化的动态类型
   * @return 构造出的动态实例*/
  public DynamicObject newInstance(DynamicClass dynamicClass){
    return newInstance(EMPTY_CLASSES, dynamicClass);
  }

  /**使用默认构造函数构造动态类的实例，实例实现了给出的接口列表，java类型委托为{@link Object}
   *
   * @param interfaces 实例实现的接口列表
   * @param dynamicClass 用于实例化的动态类型
   * @return 构造出的动态实例*/
  @SuppressWarnings("rawtypes")
  public DynamicObject newInstance(Class<?>[] interfaces, DynamicClass dynamicClass){
    return newInstance(Object.class, interfaces, dynamicClass);
  }

  /**用给出的构造函数参数构造动态类的实例，参数表必须可以在委托的java类型中存在匹配的可用构造器。
   * <p>实例无额外接口，类型委托由参数确定
   *
   * @param base 执行委托的java类型，这将决定此实例可分配到的类型
   * @param dynamicClass 用于实例化的动态类型
   * @param args 构造函数实参
   * @return 构造出的动态实例
   *
   * @throws RuntimeException 若构造函数实参无法匹配到相应的构造器或者存在其他异常*/
  public <T> DynamicObject<T> newInstance(Class<T> base, DynamicClass dynamicClass, Object... args){
    return newInstance(base, EMPTY_CLASSES, dynamicClass, args);
  }

  /**用给出的构造函数参数构造动态类的实例，参数表必须可以在委托的java类型中存在匹配的可用构造器。
   * <p>实例实现了给出的接口列表，类型委托由参数确定
   *
   * @param base 执行委托的java类型，这将决定此实例可分配到的类型
   * @param interfaces 实例实现的接口列表
   * @param dynamicClass 用于实例化的动态类型
   * @param args 构造函数实参
   * @return 构造出的动态实例
   *
   * @throws RuntimeException 若构造函数实参无法匹配到相应的构造器或者存在其他异常*/
  @SuppressWarnings("unchecked")
  public <T> DynamicObject<T> newInstance(Class<T> base, Class<?>[] interfaces, DynamicClass dynamicClass, Object... args){
    checkBase(base);

    Class<? extends T> clazz = getDynamicBase(base, interfaces);
    try{
      List<Object> argsLis = new ArrayList<>(Arrays.asList(
          dynamicClass,
          genPool(clazz, dynamicClass),
          classPoolsMap.get(clazz)
      ));
      argsLis.addAll(Arrays.asList(args));

      Constructor<?> cstr = null;
      for(Constructor<?> constructor: clazz.getDeclaredConstructors()){
        FunctionType t;
        if((t = FunctionType.from(constructor)).match(argsLis.toArray())){
          cstr = constructor;
          break;
        }
        t.recycle();
      }
      if(cstr == null)
        throw new NoSuchMethodError("no matched constructor found with parameter " + Arrays.toString(args));

      FunctionType type = FunctionType.inst(cstr.getParameterTypes());
      Constructor<?> c = cstr;
      DynamicObject<T> inst = (DynamicObject<T>) constructors.computeIfAbsent(clazz, e -> new HashMap<>())
                                                             .computeIfAbsent(type, t -> {
        try{
          return LOOKUP_INST.unreflectConstructor(c);
        }catch(IllegalAccessException e){
          throw new RuntimeException(e);
        }
      }).invokeWithArguments(argsLis.toArray());
      type.recycle();

      return inst;
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  public JavaHandleHelper getHelper(){
    return helper;
  }

  /**检查委托基类的可用性，基类若为final或者基类为接口则抛出异常*/
  protected <T> void checkBase(Class<T> base){
    if(base.isInterface())
      throw new IllegalHandleException("interface cannot use to derive a dynamic class, please write it in parameter \"interfaces\" to implement this interface");

    if(Modifier.isFinal(base.getModifiers()))
      throw new IllegalHandleException("cannot derive a dynamic class with a final class");
  }

  /**生成动态类型相应的数据池，数据池完成动态类型向上对超类的迭代逐级分配数据池信息，同时对委托产生的动态类型生成所有方法和字段的函数/变量入口并放入数据池
   *
   * @param base 动态委托类
   * @param dynamicClass 描述行为的动态类型
   * @return 生成的动态类型数据池*/
  protected <T> DataPool genPool(Class<? extends T> base, DynamicClass dynamicClass){
    DataPool basePool = classPoolsMap.computeIfAbsent(base, clazz -> {
      AtomicBoolean immutable = new AtomicBoolean();
      DataPool res = new DataPool(null){
        @Override
        public void setFunction(String name, Function<?, ?> function, Class<?>... argsType){
          if(immutable.get())
            throw new IllegalHandleException("immutable pool");

          super.setFunction(name, function, argsType);
        }

        @Override
        public void setVariable(IVariable var){
          if(immutable.get())
            throw new IllegalHandleException("immutable pool");

          super.setVariable(var);
        }
      };

      Class<?> curr = clazz;
      while(curr != null){
        if(curr.getAnnotation(DynamicType.class) != null){
          for(Method method: curr.getDeclaredMethods()){
            CallSuperMethod callSuper = method.getAnnotation(CallSuperMethod.class);
            if(callSuper != null){
              String name = callSuper.srcMethod();
              String signature = FunctionType.signature(name, method.getParameterTypes());
              res.setFunction(
                  name,
                  (self, args) -> ((SuperInvoker) self).invokeSuper(signature, args.args()),
                  method.getParameterTypes()
              );
            }
          }
          curr = curr.getSuperclass();
        }

        for(Field field: curr.getDeclaredFields()){
          if(Modifier.isStatic(field.getModifiers()) || isInternalField(field.getName())) continue;

          helper.makeAccess(field);
          res.setVariable(helper.genJavaVariableRef(field, res));
        }
        curr = curr.getSuperclass();
      }

      immutable.set(true);

      return res;
    });

    return dynamicClass.genPool(basePool);
  }

  private static boolean isInternalField(String name){
    return INTERNAL_FIELD.contains(name);
  }

  /**根据委托基类和实现的接口获取生成的动态类型实例的类型，类型会生成并放入池，下一次获取会直接从池中取出该类型
   *
   * @param base 委托的基类
   * @param interfaces 需要实现的接口列表*/
  @SuppressWarnings("unchecked")
  protected <T> Class<? extends T> getDynamicBase(Class<T> base, Class<?>[] interfaces){
    return (Class<? extends T>) classPool.computeIfAbsent(new ClassImplements<>(base, interfaces), e -> {
      Class<?> c = base;

      while (c != null){
        helper.makeAccess(c);
        c = c.getSuperclass();
      }

      return generateClass(base, interfaces);
    });
  }

  /**由基类与接口列表建立动态类的打包名称，打包名称具有唯一性（或者足够高的离散性，不应出现频繁的碰撞）和不变性
   *
   * @param baseClass 基类
   * @param interfaces 接口列表
   * @return 由基类名称与全部接口名称的哈希值构成的打包名称*/
  public static <T> String getDynamicName(Class<T> baseClass, Class<?>... interfaces){
    return ensurePackage(baseClass.getName()) + "$dynamic$" + FunctionType.typeNameHash(interfaces);
  }

  private static String ensurePackage(String name){
    if(name.startsWith("java.")){
      return name.replaceFirst("java\\.", "lava.");
    }
    return name;
  }

  /**对动态委托产生的类型进行继续委托创建代码的方法，应当将首要处理过程向上传递给超类。
   * 与{@link DynamicMaker#makeClassInfo(Class, Class[])}的行为相似，但需要将部分行为适应到已经具备委托行为的超类*/
  @SuppressWarnings({"unchecked"})
  protected <T> ClassInfo<? extends T> makeClassInfoOnDynmaic(Class<T> baseClass, Class<?>[] interfaces){
    ArrayList<ClassInfo<?>> inter = new ArrayList<>(interfaces.length);
    for(Class<?> i: interfaces){
      inter.add(asType(i));
    }

    ClassInfo<? extends T> classInfo = new ClassInfo<>(
        Modifier.PUBLIC,
        ensurePackage(baseClass.getName()) + "$" + FunctionType.typeNameHash(interfaces),
        asType(baseClass),
        inter.toArray(new ClassInfo[0])
    );
    FieldInfo<HashMap> methodIndex = classInfo.declareField(
        Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
        "$methodIndex$",
        HASH_MAP_TYPE,
        null
    );

    CodeBlock<Void> clinit = classInfo.getClinitBlock();
    ILocal<HashMap> caseIndex = clinit.local(HASH_MAP_TYPE);
    clinit.newInstance(
        asType(HashMap.class).getConstructor(),
        caseIndex
    );
    clinit.assign(null, caseIndex, methodIndex);
    ILocal<Integer> tempInt = clinit.local(INT_TYPE);
    ILocal<Integer> tempIndexWrap = clinit.local(INTEGER_CLASS_TYPE);
    ILocal<String> tempSign = clinit.local(STRING_TYPE);

    ILocal<FunctionType> tempType = clinit.local(FUNCTION_TYPE_TYPE);
    ILocal<Class> tempClass = clinit.local(CLASS_TYPE);
    ILocal<Class[]> tempClasses = clinit.local(CLASS_TYPE.asArray());


    HashMap<Method, Integer> callSuperCaseMap = new HashMap<>();

    // public <init>(*parameters*){
    //   super(*parameters*);
    // }
    for(Constructor<?> cstr: baseClass.getDeclaredConstructors()){
      if((cstr.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) continue;
      if(Modifier.isFinal(cstr.getModifiers())) continue;

      ArrayList<IClass<?>> args = new ArrayList<>();
      for(Class<?> type: cstr.getParameterTypes()){
        args.add(ClassInfo.asType(type));
      }
      IClass<?>[] argArr = args.toArray(new IClass[0]);
      IMethod<?, Void> constructor = classInfo.superClass().getConstructor(argArr);

      CodeBlock<Void> code = classInfo.declareConstructor(Modifier.PUBLIC, Parameter.trans(argArr));
      code.invokeSuper(code.getThis(), constructor, null, code.getParamList().toArray(new ILocal<?>[0]));
    }

    {//interfaces
      Stack<Class<?>> interfaceStack = new Stack<>();
      Class<?> curr = baseClass;
      INTERFACE_TEMP.clear();
      while(curr != null || !interfaceStack.empty()){
        if(curr != null){
          for(Class<?> i: curr.getInterfaces()){
            if(INTERFACE_TEMP.add(i)) interfaceStack.push(i);
          }
        }else curr = interfaceStack.pop();

        if(!curr.isInterface()){
          curr = curr.getSuperclass();
        }
        else curr = null;
      }

      interfaceStack.addAll(Arrays.asList(interfaces));
      while(!interfaceStack.empty()){
        Class<?> interfaceCurr = interfaceStack.pop();
        for(Class<?> i: interfaceCurr.getInterfaces()){
          if(INTERFACE_TEMP.add(i)) interfaceStack.push(i);
        }

        for(Method method: interfaceCurr.getMethods()){
          if(Modifier.isStatic(method.getModifiers())) continue;

          ClassInfo<?> typeClass = ClassInfo.asType(interfaceCurr);
          MethodInfo<?, ?> superMethod;

          String methodName = method.getName();
          ClassInfo<?> returnType = asType(method.getReturnType());

          if(OVERRIDES.computeIfAbsent(methodName, e -> new HashSet<>()).add(FunctionType.from(method))){
            superMethod = !Modifier.isAbstract(method.getModifiers()) && method.isDefault()? typeClass.getMethod(
                returnType,
                methodName,
                Arrays.stream(method.getParameterTypes()).map(ClassInfo::asType).toArray(ClassInfo[]::new)
            ): null;

            callSuperCaseMap.put(method, callSuperCaseMap.size());

            String typeF = methodName + "$" + FunctionType.typeNameHash(method.getParameterTypes());
            FieldInfo<FunctionType> funType = classInfo.declareField(
                Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
                typeF,
                FUNCTION_TYPE_TYPE,
                null
            );

            // private static final FunctionType FUNCTION_TYPE$*name*;
            // static {
            //   ...
            //   FUNCTION_TYPE$*signature* = FunctionType.as(*paramTypes*);
            //   methodIndex.put(*signature*, *index*);
            //   ...
            // }
            {
              String signature = FunctionType.signature(method);
              clinit.loadConstant(tempInt, method.getParameterCount());
              clinit.newArray(
                  CLASS_TYPE,
                  tempClasses,
                  tempInt
              );
              Class<?>[] paramTypes = method.getParameterTypes();
              for(int i=0; i<paramTypes.length; i++){
                clinit.loadConstant(tempClass, paramTypes[i]);
                clinit.loadConstant(tempInt, i);

                clinit.arrayPut(tempClasses, tempInt, tempClass);
              }

              clinit.invoke(
                  null,
                  TYPE_INST,
                  tempType,
                  tempClasses
              );

              clinit.assign(null, tempType, funType);

              clinit.loadConstant(tempSign, signature);
              clinit.loadConstant(tempInt, callSuperCaseMap.get(method));

              clinit.invoke(
                  null,
                  VALUE_OF,
                  tempIndexWrap,
                  tempInt
              );

              clinit.invoke(
                  caseIndex,
                  MAP_PUT,
                  null,
                  tempSign,
                  tempIndexWrap
              );
            }

            // public *returnType* *name*(*parameters*){
            //   *[return]* this.invokeFunc(FUNCTION_TYPE$*signature* ,"*name*", parameters);
            // }
            {
              CodeBlock<?> code = classInfo.declareMethod(
                  Modifier.PUBLIC,
                  methodName,
                  returnType,
                  Parameter.asParameter(method.getParameters())
              );

              ILocal<FunctionType> type = code.local(FUNCTION_TYPE_TYPE);
              ILocal<String> met = code.local(STRING_TYPE);

              code.assign(null, funType, type);

              code.loadConstant(met, method.getName());

              ILocal<Object[]> argList = code.local(OBJECT_TYPE.asArray());
              ILocal<Integer> length = code.local(INT_TYPE);
              code.loadConstant(length, method.getParameterCount());
              code.invoke(null, GET_LIST, argList, length);

              if(method.getParameterCount() > 0){
                ILocal<Integer> index = code.local(INT_TYPE);
                for(int i = 0; i < code.getParamList().size(); i++){
                  code.loadConstant(index, i);
                  code.arrayPut(argList, index, code.getRealParam(i));
                }
              }

              if(returnType != VOID_TYPE){
                ILocal res = code.local(returnType);
                code.invoke(code.getThis(), INVOKE, res, type, met, argList);
                code.invoke(null, RECYCLE_LIST, null, argList);
                code.returnValue(res);
              }
              else{
                code.invoke(code.getThis(), INVOKE, null, type, met, argList);
                code.invoke(null, RECYCLE_LIST, null, argList);
              }
            }

            // private final *returnType* *name*$super(*parameters*){
            //   *[return]* super.*name*(*parameters*);
            // }
            if(superMethod != null){
              if (Modifier.isInterface(superMethod.owner().modifiers())){
                IClass<?> c = classInfo.superClass();
                boolean found = false;
                t: while (c != null && c != OBJECT_TYPE){
                  for (IClass<?> interf: c.interfaces()) {
                    if (interf == superMethod.owner()){
                      found = true;
                      break t;
                    }
                  }

                  c = c.superClass();
                }

                if (found){
                  superMethod = new MethodInfo<>(
                      c,
                      Modifier.PUBLIC,
                      superMethod.name(),
                      superMethod.returnType(),
                      superMethod.parameters().toArray(new Parameter[0])
                  );
                }
              }

              CodeBlock<?> code = classInfo.declareMethod(
                  Modifier.PRIVATE | Modifier.FINAL,
                  methodName + CALLSUPER,
                  returnType,
                  Parameter.asParameter(method.getParameters())
              );

              if(returnType != VOID_TYPE){
                ILocal res = code.local(returnType);
                code.invokeSuper(code.getThis(), superMethod, res, code.getParamList().toArray(LOCALS_EMP));
                code.returnValue(res);
              }
              else code.invokeSuper(code.getThis(), superMethod, null, code.getParamList().toArray(LOCALS_EMP));

              AnnotationType<CallSuperMethod> callSuper = AnnotationType.asAnnotationType(CallSuperMethod.class);
              HashMap<String, Object> map = new HashMap<>();
              map.put("srcMethod", methodName);
              callSuper.annotateTo(code.owner(), map);
            }
          }
        }
      }
    }

    //switch super
    // public Object invokeSuper(String signature, Object... args);{
    //   Integer ind = methodIndex.get(signature);
    //   if(ind == null) return super.invokeSuper(signature, args)
    //   switch(ind){
    //     ...
    //     case *index*: *method*$super(args[0], args[1],...); break;
    //     ...
    //   }
    // }
    {
      CodeBlock<Object> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "invokeSuper",
          OBJECT_TYPE,
          Parameter.trans(
              STRING_TYPE,
              OBJECT_TYPE.asArray()
          )
      );

      IMethod<T, Object> superCaller = (IMethod<T, Object>) classInfo.superClass().getMethod(code.owner().returnType(), code.owner().name(),
          code.owner().parameters().stream().map(Parameter::getType).toArray(IClass[]::new));

      ILocal<Integer> index = code.local(INT_TYPE);
      ILocal<Integer> indexWrap = code.local(INTEGER_CLASS_TYPE);
      ILocal<Object> obj = code.local(OBJECT_TYPE);
      ILocal<Object> emp = code.local(OBJECT_TYPE);
      ILocal<HashMap> caseMap = code.local(HASH_MAP_TYPE);
      code.assign(null, methodIndex, caseMap);
      code.invoke(
          caseMap,
          MAP_GET,
          obj,
          code.getRealParam(0)
      );
      Label j = code.label();
      code.loadConstant(emp, null);
      code.compare(obj, ICompare.Comparison.UNEQUAL, emp, j);

      code.invokeSuper(code.getThis(), superCaller, obj, code.getParamList().toArray(new ILocal<?>[0]));
      code.returnValue(obj);

      code.markLabel(j);
      code.cast(obj, indexWrap);
      code.invoke(
          indexWrap,
          INTEGER_CLASS_TYPE.getMethod(INT_TYPE, "intValue"),
          index
      );

      Label end = code.label();

      ISwitch<Integer> iSwitch = code.switchDef(index, end);

      ILocal<Object[]> args = code.getRealParam(1);
      makeSwitch(callSuperCaseMap, code, obj, end, iSwitch, args);
    }

    return classInfo;
  }

  /**创建动态实例类型的类型标识，这应当覆盖所有委托目标类的方法和实现的接口中的方法，若超类的某一成员方法不是抽象的，需保留对超类方法的入口，
   * 再重写本方法，对超类方法的入口需要有一定的标识以供生成基类数据池的引用函数时使用。
   * <p>对于给定的基类和接口列表，生成的动态实例基类的名称是唯一的（或者足够的离散以至于几乎不可能碰撞）。
   * <p>关于此方法实现需要完成的工作，请参阅{@link DynamicObject}中的描述
   *
   * @param baseClass 委托基类
   * @param interfaces 实现的接口列表
   * @return 完成了所有必要描述的类型标识*/
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected <T> ClassInfo<? extends T> makeClassInfo(Class<T> baseClass, Class<?>[] interfaces){
    if(baseClass.getAnnotation(DynamicType.class) != null)
      return makeClassInfoOnDynmaic(baseClass, interfaces);

    ArrayList<ClassInfo<?>> inter = new ArrayList<>(interfaces.length + 1);
    inter.add(asType(DynamicObject.class));
    inter.add(asType(SuperInvoker.class));

    for(Class<?> i: interfaces){
      inter.add(asType(i));
    }

    ClassInfo<? extends T> classInfo = new ClassInfo<>(
        Modifier.PUBLIC,
        getDynamicName(baseClass, interfaces),
        asType(baseClass),
        inter.toArray(new ClassInfo[0])
    );

    FieldInfo<DynamicClass> dyType = classInfo.declareField(
        Modifier.PRIVATE | Modifier.FINAL,
        "$dynamic_type$",
        DYNAMIC_CLASS_TYPE,
        null
    );
    FieldInfo<DataPool> dataPool = classInfo.declareField(
        Modifier.PRIVATE | Modifier.FINAL,
        "$datapool$",
        DATA_POOL_TYPE,
        null
    );
    FieldInfo<HashMap<String, Object>> varPool = (FieldInfo) classInfo.declareField(
        Modifier.PRIVATE | Modifier.FINAL,
        "$varValuePool$",
        HASH_MAP_TYPE,
        null
    );
    FieldInfo<DataPool.ReadOnlyPool> basePoolPointer = classInfo.declareField(
        Modifier.PRIVATE | Modifier.FINAL,
        "$superbasepointer$",
        READONLY_POOL_TYPE,
        null
    );
    FieldInfo<HashMap> methodIndex = classInfo.declareField(
        Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
        "$methodIndex$",
        HASH_MAP_TYPE,
        null
    );

    CodeBlock<Void> clinit = classInfo.getClinitBlock();
    ILocal<HashMap> caseIndex = clinit.local(HASH_MAP_TYPE);
    clinit.newInstance(
        asType(HashMap.class).getConstructor(),
        caseIndex
    );
    clinit.assign(null, caseIndex, methodIndex);
    ILocal<Integer> tempInt = clinit.local(INT_TYPE);
    ILocal<Integer> tempIndexWrap = clinit.local(INTEGER_CLASS_TYPE);
    ILocal<String> tempSign = clinit.local(STRING_TYPE);

    ILocal<FunctionType> tempType = clinit.local(FUNCTION_TYPE_TYPE);
    ILocal<Class> tempClass = clinit.local(CLASS_TYPE);
    ILocal<Class[]> tempClasses = clinit.local(CLASS_TYPE.asArray());

    HashMap<Method, Integer> callSuperCaseMap = new HashMap<>();

    // public <init>(DynamicClass $dyC$, DataPool $datP$, DataPool.ReadOnlyPool $basePool$, *parameters*){
    //   this.$dynamic_type$ = $dyC$;
    //   this.$datapool$ = $datP$;
    //   this.$varValuePool$ = new HashMap<>();
    //   super(*parameters*);
    //   this.$superbasepointer$ = $datapool$.getReader(this);
    //
    //   this.$datapool$.init(this, *parameters*);
    // }
    for(Constructor<?> cstr: baseClass.getDeclaredConstructors()){
      if((cstr.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) continue;
      if(Modifier.isFinal(cstr.getModifiers())) continue;

      List<Parameter<?>> params = new ArrayList<>(Arrays.asList(Parameter.as(
          0, DynamicClass.class, "$dyc$",
          0, DataPool.class, "$datP$",
          0, DataPool.class, "$basePool$"
      )));
      List<Parameter<?>> superParams = Arrays.asList(Parameter.asParameter(cstr.getParameters()));
      params.addAll(superParams);

      IMethod<?, Void> constructor =classInfo.superClass().getConstructor(superParams.stream().map(Parameter::getType).toArray(IClass[]::new));

      CodeBlock<Void> code = classInfo.declareConstructor(Modifier.PUBLIC, params.toArray(new Parameter[0]));
      List<ILocal<?>> l = code.getParamList();
      ILocal<?> self = code.getThis();

      ILocal<DynamicClass> dyC = code.getParam(1);
      ILocal<DataPool> datP = code.getParam(2);
      code.assign(self, dyC, dyType);
      code.assign(self, datP, dataPool);

      ILocal<HashMap> map = code.local(HASH_MAP_TYPE);
      code.newInstance(HASH_MAP_TYPE.getConstructor(), map);
      code.assign(self, map, varPool);

      code.invokeSuper(self, constructor, null, l.subList(3, l.size()).toArray(LOCALS_EMP));

      ILocal<DataPool.ReadOnlyPool> base = code.local(READONLY_POOL_TYPE);
      code.invoke(code.getParam(3), GET_READER, base, self);
      code.assign(self, base, basePoolPointer);

      ILocal<Object[]> argList = code.local(OBJECT_TYPE.asArray());
      ILocal<Integer> length = code.local(INT_TYPE);
      code.loadConstant(length, cstr.getParameterCount());
      code.invoke(null, GET_LIST, argList, length);
      if(cstr.getParameterCount() > 0){
        ILocal<Integer> index = code.local(INT_TYPE);
        for(int i = 3; i < code.getParamList().size(); i++){
          code.loadConstant(index, i - 3);
          code.arrayPut(argList, index, code.getRealParam(i));
        }
      }
      code.invoke(datP, INIT, null, self, argList);

      code.invoke(null, RECYCLE_LIST, null, argList);
    }

    OVERRIDES.clear();
    INTERFACE_TEMP.clear();

    Stack<Class<?>> interfaceStack = new Stack<>();
    HashMap<String, HashSet<FunctionType>> overrideMethods = new HashMap<>();
    HashMap<String, HashSet<FunctionType>> finalMethods = new HashMap<>();

    Class<?> curr = baseClass;

    ClassInfo<?> typeClass;
    MethodInfo<?, ?> superMethod;
    while(curr != null || !interfaceStack.empty()){
      if(curr != null){
        for(Class<?> i: curr.getInterfaces()){
          if(INTERFACE_TEMP.add(i)) interfaceStack.push(i);
        }
      }
      else curr = interfaceStack.pop();

      typeClass = asType(curr);
      for(Method method: curr.getDeclaredMethods()){
        if (!filterMethod(overrideMethods, finalMethods, method)) continue;

        String methodName = method.getName();
        ClassInfo<?> returnType = asType(method.getReturnType());

        if(OVERRIDES.computeIfAbsent(methodName, e -> new HashSet<>()).add(FunctionType.from(method))){
          superMethod = !Modifier.isAbstract(method.getModifiers()) || (curr.isInterface() && method.isDefault())? typeClass.getMethod(
              returnType,
              methodName,
              Arrays.stream(method.getParameterTypes()).map(ClassInfo::asType).toArray(ClassInfo[]::new)
          ): null;

          callSuperCaseMap.put(method, callSuperCaseMap.size());

          String typeF = methodName + "$" + FunctionType.typeNameHash(method.getParameterTypes());
          FieldInfo<FunctionType> funType = classInfo.declareField(
              Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
              typeF,
              FUNCTION_TYPE_TYPE,
              null
          );

          // private static final FunctionType FUNCTION_TYPE$*name*;
          // static {
          //   ...
          //   FUNCTION_TYPE$*signature* = FunctionType.as(*paramTypes*);
          //   methodIndex.put(*signature*, *index*);
          //   ...
          // }
          {
            String signature = FunctionType.signature(method);
            clinit.loadConstant(tempInt, method.getParameterCount());
            clinit.newArray(
                CLASS_TYPE,
                tempClasses,
                tempInt
            );
            Class<?>[] paramTypes = method.getParameterTypes();
            for(int i=0; i<paramTypes.length; i++){
              clinit.loadConstant(tempClass, paramTypes[i]);
              clinit.loadConstant(tempInt, i);

              clinit.arrayPut(tempClasses, tempInt, tempClass);
            }

            clinit.invoke(
                null,
                TYPE_INST,
                tempType,
                tempClasses
            );

            clinit.assign(null, tempType, funType);

            clinit.loadConstant(tempSign, signature);
            clinit.loadConstant(tempInt, callSuperCaseMap.get(method));

            clinit.invoke(
                null,
                VALUE_OF,
                tempIndexWrap,
                tempInt
            );

            clinit.invoke(
                caseIndex,
                MAP_PUT,
                null,
                tempSign,
                tempIndexWrap
            );
          }

          // public *returnType* *name*(*parameters*){
          //   *[return]* this.invokeFunc(FUNCTION_TYPE$*signature* ,"*name*", parameters);
          // }
          {
            CodeBlock<?> code = classInfo.declareMethod(
                Modifier.PUBLIC,
                methodName,
                returnType,
                Parameter.asParameter(method.getParameters())
            );

            ILocal<FunctionType> type = code.local(FUNCTION_TYPE_TYPE);
            ILocal<String> met = code.local(STRING_TYPE);

            code.assign(null, funType, type);

            code.loadConstant(met, method.getName());

            ILocal<Object[]> argList = code.local(OBJECT_TYPE.asArray());
            ILocal<Integer> length = code.local(INT_TYPE);
            code.loadConstant(length, method.getParameterCount());
            code.invoke(null, GET_LIST, argList, length);

            if(method.getParameterCount() > 0){
              ILocal<Integer> index = code.local(INT_TYPE);
              for(int i = 0; i < code.getParamList().size(); i++){
                code.loadConstant(index, i);
                code.arrayPut(argList, index, code.getRealParam(i));
              }
            }

            if(returnType != VOID_TYPE){
              ILocal res = code.local(returnType);
              code.invoke(code.getThis(), INVOKE, res, type, met, argList);
              code.invoke(null, RECYCLE_LIST, null, argList);
              code.returnValue(res);
            }
            else{
              code.invoke(code.getThis(), INVOKE, null, type, met, argList);
              code.invoke(null, RECYCLE_LIST, null, argList);
            }
          }

          // private final *returnType* *name*$super(*parameters*){
          //   *[return]* super.*name*(*parameters*);
          // }
          if(superMethod != null){
            if (Modifier.isInterface(superMethod.owner().modifiers())){
              IClass<?> c = classInfo.superClass();
              boolean found = false;
              t: while (c != null && c != OBJECT_TYPE){
                for (IClass<?> interf: c.interfaces()) {
                  if (interf == superMethod.owner()){
                    found = true;
                    break t;
                  }
                }

                c = c.superClass();
              }

              if (found){
                superMethod = new MethodInfo<>(
                    c,
                    Modifier.PUBLIC,
                    superMethod.name(),
                    superMethod.returnType(),
                    superMethod.parameters().toArray(new Parameter[0])
                );
              }
            }

            CodeBlock<?> code = classInfo.declareMethod(
                Modifier.PRIVATE | Modifier.FINAL,
                methodName + CALLSUPER,
                returnType,
                Parameter.asParameter(method.getParameters())
            );

            if(returnType != VOID_TYPE){
              ILocal res = code.local(returnType);
              code.invokeSuper(code.getThis(), superMethod, res, code.getParamList().toArray(LOCALS_EMP));
              code.returnValue(res);
            }
            else code.invokeSuper(code.getThis(), superMethod, null, code.getParamList().toArray(LOCALS_EMP));

            AnnotationType<CallSuperMethod> callSuper = AnnotationType.asAnnotationType(CallSuperMethod.class);
            HashMap<String, Object> map = new HashMap<>();
            map.put("srcMethod", methodName);
            callSuper.annotateTo(code.owner(), map);
          }
        }
      }

      if(!curr.isInterface()){
        curr = curr.getSuperclass();
      }
      else curr = null;
    }

    // public Object invokeSuper(String signature, Object... args);{
    //   switch(methodIndex.get(signature)){
    //     ...
    //     case *index*: *method*$super(args[0], args[1],...); break;
    //     ...
    //   }
    // }
    {
      CodeBlock<Object> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "invokeSuper",
          OBJECT_TYPE,
          Parameter.trans(
              STRING_TYPE,
              OBJECT_TYPE.asArray()
          )
      );

      ILocal<Integer> index = code.local(INT_TYPE);
      ILocal<Integer> indexWrap = code.local(INTEGER_CLASS_TYPE);
      ILocal<Object> obj = code.local(OBJECT_TYPE);
      ILocal<HashMap> caseMap = code.local(HASH_MAP_TYPE);
      code.assign(null, methodIndex, caseMap);
      code.invoke(
          caseMap,
          MAP_GET,
          obj,
          code.getRealParam(0)
      );
      code.cast(obj, indexWrap);
      code.invoke(
          indexWrap,
          INTEGER_CLASS_TYPE.getMethod(INT_TYPE, "intValue"),
          index
      );

      Label end = code.label();

      ISwitch<Integer> iSwitch = code.switchDef(index, end);

      ILocal<Object[]> args = code.getRealParam(1);
      ILocal<Object> tmpObj = code.local(OBJECT_TYPE);
      makeSwitch(callSuperCaseMap, code, tmpObj, end, iSwitch, args);
    }

    // public DataPool.ReadOnlyPool baseSuperPool(){
    //   return this.$superbasepointer$;
    // }
    {
      CodeBlock<DataPool.ReadOnlyPool> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "baseSuperPointer",
          READONLY_POOL_TYPE
      );
      ILocal<DataPool.ReadOnlyPool> res = code.local(READONLY_POOL_TYPE);
      code.assign(code.getThis(), basePoolPointer, res);
      code.returnValue(res);
    }

    // public DynamicClass<Self> getDyClass(){
    //   return this.$dynamic_type$;
    // }
    {
      CodeBlock<DynamicClass> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "getDyClass",
          DYNAMIC_CLASS_TYPE
      );
      ILocal<DynamicClass> res = code.local(DYNAMIC_CLASS_TYPE);
      code.assign(code.getThis(), dyType, res);
      code.returnValue(res);
    }

    // public <T> T varValueGet(String name){
    //   return this.$varValuePool$.get(name);
    // }
    {
      CodeBlock<Object> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "varValueGet",
          OBJECT_TYPE,
          Parameter.trans(STRING_TYPE)
      );
      ILocal<HashMap> map = code.local(HASH_MAP_TYPE);
      ILocal<Object> res = code.local(OBJECT_TYPE);
      code.assign(code.getThis(), varPool, map);
      code.invoke(map, MAP_GET, res, code.getRealParam(0));
      code.returnValue(res);
    }

    // public <T> varValueSet(String name, Object value){
    //   return this.$varValuePool$.put(name, value);
    // }
    {
      CodeBlock<Void> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "varValueSet",
          VOID_TYPE,
          Parameter.trans(
              STRING_TYPE,
              OBJECT_TYPE
          )
      );
      ILocal<HashMap> map = code.local(HASH_MAP_TYPE);
      code.assign(code.getThis(), varPool, map);
      code.invoke(map, MAP_PUT, null, code.getRealParam(0), code.getRealParam(1));
    }

    // public IVariable getVariable(String name){
    //   return this.$datapool$.getVariable(name);
    // }
    {
      CodeBlock<IVariable> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "getVariable",
          VAR_TYPE,
          Parameter.as(0, STRING_TYPE, "name")
      );
      ILocal<DataPool> pool = code.local(DATA_POOL_TYPE);
      code.assign(code.getThis(), dataPool, pool);

      ILocal<IVariable> result = code.local(VAR_TYPE);
      code.invoke(pool, GET_VAR, result, code.getParam(1));
      code.returnValue(result);
    }

    // public <T> void setVariable(IVariable var){
    //   this.$datapool$.setVariable(var);
    // }
    {
      CodeBlock<Void> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "setVariable",
          VOID_TYPE,
          Parameter.as(0, VAR_TYPE, "var")
      );
      ILocal<DataPool> pool = code.local(DATA_POOL_TYPE);
      code.assign(code.getThis(), dataPool, pool);

      code.invoke(pool, SET_VAR, null, code.getParam(1));
      code.returnVoid();
    }

    // public IFunctionEntry getFunc(String name, FunctionType type){
    //   return this.$datapool$.select(name, type);
    // }
    {
      CodeBlock<IFunctionEntry> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "getFunc",
          FUNC_ENTRY_TYPE,
          Parameter.as(
              0, STRING_TYPE, "name",
              0, FUNCTION_TYPE_TYPE, "type"
          )
      );
      ILocal<DataPool> pool = code.local(DATA_POOL_TYPE);
      code.assign(code.getThis(), dataPool, pool);

      ILocal<IFunctionEntry> fnc = code.local(FUNC_ENTRY_TYPE);
      code.invoke(pool, SELECT, fnc, code.getParam(1), code.getParam(2));
      code.returnValue(fnc);
    }

    // public <R> void setFunc(String name, Function<Self, R> func, Class<?>... argTypes){
    //   this.$datapool$.set(name, func, argTypes);
    // }
    {
      CodeBlock<Void> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "setFunc",
          VOID_TYPE,
          Parameter.as(
              0, STRING_TYPE, "name",
              0, FUNCTION_TYPE, "func",
              0, CLASS_TYPE.asArray(), "argTypes"
          )
      );

      ILocal<DataPool> pool = code.local(DATA_POOL_TYPE);
      code.assign(code.getThis(), dataPool, pool);

      code.invoke(pool, SETFUNC, null, code.getParam(1), code.getParam(2), code.getParam(3));
    }

    // public <R> void setFunc(String name, Function<Self, R> func, Class<?>... argTypes){
    //   this.$datapool$.set(name, func, argTypes);
    // }
    {
      CodeBlock<Void> code = classInfo.declareMethod(
          Modifier.PUBLIC,
          "setFunc",
          VOID_TYPE,
          Parameter.as(
              0, STRING_TYPE, "name",
              0, SUPER_GET_FUNC_TYPE, "func",
              0, CLASS_TYPE.asArray(), "argTypes"
          )
      );

      ILocal<DataPool> pool = code.local(DATA_POOL_TYPE);
      code.assign(code.getThis(), dataPool, pool);

      code.invoke(pool, SETFUNC2, null, code.getParam(1), code.getParam(2), code.getParam(3));
    }

    AnnotationType<DynamicType> dycAnno = AnnotationType.asAnnotationType(DynamicType.class);
    dycAnno.annotateTo(classInfo, null);

    return classInfo;
  }

  private static boolean filterMethod(HashMap<String, HashSet<FunctionType>> overrideMethods, HashMap<String, HashSet<FunctionType>> finalMethods, Method method) {
    //对于已经被声明为final的方法将被添加到排除列表
    if (Modifier.isFinal(method.getModifiers())){
      finalMethods.computeIfAbsent(method.getName(), e -> new HashSet<>()).add(FunctionType.from(method));
      return false;
    }

    // 如果方法是静态的，或者方法不对子类可见则不重写此方法
    if(Modifier.isStatic(method.getModifiers())) return false;
    if((method.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) return false;

    return !finalMethods.computeIfAbsent(method.getName(), e -> new HashSet<>()).contains(FunctionType.from(method))
        && overrideMethods.computeIfAbsent(method.getName(), e -> new HashSet<>()).add(FunctionType.from(method));
  }

  @SuppressWarnings("unchecked")
  protected void makeSwitch(HashMap<Method, Integer> callSuperCaseMap, CodeBlock<Object> code, ILocal<Object> obj, Label end, ISwitch<Integer> iSwitch, ILocal<Object[]> args){
    ILocal<Integer> tmpInd = code.local(INT_TYPE);

    for(Map.Entry<Method, Integer> entry: callSuperCaseMap.entrySet()){
      Label l = code.label();
      code.markLabel(l);

      iSwitch.addCase(entry.getValue(), l);

      Method m = entry.getKey();
      IMethod method = code.owner().owner().getMethod(
          asType(m.getReturnType()),
          m.getName() + CALLSUPER,
          Arrays.stream(m.getParameterTypes()).map(ClassInfo::asType).toArray(IClass[]::new)
      );
      ILocal<?>[] params = new ILocal[method.parameters().size()];
      for(int in = 0; in < params.length; in++){
        params[in] = code.local(((Parameter)method.parameters().get(in)).getType());
        code.loadConstant(tmpInd, in);
        code.arrayGet(args, tmpInd, obj);
        code.cast(obj, params[in]);
      }

      code.invoke(code.getThis(), method, obj, params);
      if(method.returnType() == VOID_TYPE){
        code.loadConstant(obj, null);
      }
      code.returnValue(obj);
    }
    code.markLabel(end);

    ILocal<String> message = code.local(STRING_TYPE);
    ILocal<IllegalStateException> exception = code.local(STATE_EXCEPTION_TYPE);
    code.loadConstant(message, "no such method signature with ");
    code.operate(message, IOperate.OPCode.ADD, code.getRealParam(0), message);
    code.newInstance(
        STATE_EXCEPTION_TYPE.getConstructor(STRING_TYPE),
        exception,
        message
    );
    code.thr(exception);
  }

  /**生成委托自基类并实现了给出的接口列表的类型，而类的行为描述请参考{@link DynamicMaker#makeClassInfo(Class, Class[])}，类型描述会在此方法产出。
   * <p>该方法需要做的事通常是将makeClassInfo获得的类型标识进行生成并加载其表示的java类型
   *
   * @param baseClass 委托基类
   * @param interfaces 实现的接口列表
   * @return 对全方法进行动态委托的类型*/
  protected abstract <T> Class<? extends T> generateClass(Class<T> baseClass, Class<?>[] interfaces);

  /**动态委托类型标识，由此工厂生成的动态委托类型都会具有此注解标识*/
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @interface DynamicType{}

  /**超类方法标识，带有一个属性描述了此方法所引用的超类方法名称，所有生成的对super方法入口都会具有此注解。*/
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface CallSuperMethod{
    /**方法所调用的超类源方法，将提供给初级数据池标识对超类方法的引用*/
    String srcMethod();
  }

  public interface SuperInvoker{
    Object invokeSuper(String signature, Object... args);
  }
}
