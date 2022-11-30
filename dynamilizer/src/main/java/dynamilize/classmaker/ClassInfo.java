package dynamilize.classmaker;

import dynamilize.IllegalHandleException;
import dynamilize.classmaker.code.Element;
import dynamilize.classmaker.code.IClass;
import dynamilize.classmaker.code.IField;
import dynamilize.classmaker.code.IMethod;
import dynamilize.classmaker.code.annotation.AnnotatedElement;
import dynamilize.classmaker.code.annotation.AnnotationType;
import dynamilize.classmaker.code.annotation.IAnnotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.*;
import java.util.*;

/**类型标识，用于标记/生成一个类对象，通常有以下两种：
 * <ul>
 *   <li><strong>生成类型标识</strong>生成类型标识在默认情况下（完成类型生成之前）是可变的，用于声明和描述一个新的类型并加载它，是生成一个新的类使用的描述类型
 *   <li><strong>已有类型标识</strong>对已有类型创建的类型标识，不可变，仅用于标记一个已存在的类型以供标记分配和使用
 * </ul>
 * 默认构造的类型标记即为生成类型标识，允许声明方法和字段以及构造函数和静态代码快（cinit块）,若生成类型标识完成生成，将转变为对生成的类型的已有类型标识。
 * <p>你将需要以类似汇编语言的思路来进行行为描述，如使用goto代替for和if.
 *
 * <p>举一个简单的例子：
 * <pre>{@code
 * 有一个类：
 * public class Demo{
 *   public static String INFO = "HelloWorld";
 *
 *   public static main(String[] args){
 *     if(System.nanoTime() > 123456789){
 *       System.out.println(INFO);
 *     }
 *     else System.out.println("Late")
 *   }
 * }
 *
 * 要生成一个等效类型，应做如下描述：
 *
 *
 * }</pre>
 * 这样的过程是繁琐的，但是也是快速的，跳过编译器产生类文件牺牲了可操作性以换取了类的生成速度，建议将行为描述为模板后再基于模板进行变更以提高开发效率*/
public class ClassInfo<T> extends AnnotatedMember implements IClass<T>{
  public static final LinkedList<IClass<?>> QUEUE = new LinkedList<>();
  public static final HashSet<IClass<?>> EXCLUDE = new HashSet<>();
  private static final Map<Class<?>, ClassInfo<?>> classMap = new HashMap<>();

  private static final String OBJECTTYPEMARK = "Ljava/lang/Object;";
  private static final String INIT = "<init>";
  private static final String CINIT = "<clinit>";

  private static final int CLASS_ACCESS_MODIFIERS =
      Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
      Modifier.FINAL | Modifier.STATIC | Modifier.INTERFACE |
      Modifier.ABSTRACT | 4096/*synthetic*/ | 8192/*annotation*/ | 16384/*enum*/;

  private static final int METHOD_ACCESS_MODIFIERS =
      Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
      Modifier.FINAL | Modifier.STATIC | Modifier.NATIVE |
      Modifier.SYNCHRONIZED | Modifier.STRICT | Modifier.ABSTRACT |
      128/*varargs*/ | 4096/*synthetic*/;

  private static final int FIELD_ACCESS_MODIFIERS =
      Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
      Modifier.FINAL | Modifier.STATIC |
      Modifier.TRANSIENT | Modifier.VOLATILE;

  public static final String PRIMITIVE_REAL = "IFJZBCDV";

  /**对于非现有类型标识，此字段通常情况下为null，在完成类的创建和加载后应正确设置为产生的类
   * <p>作为已有类型的标识符则一定不为空*/
  private Class<T> clazz;

  private CodeBlock<Void> clinit;

  private String realName;

  ClassInfo<? super T> superClass;
  List<ClassInfo<?>> interfaces;
  List<Element> elements;

  Map<String, IField<?>> fieldMap;
  Map<String, IMethod<?, ?>> methodMap;

  private ClassInfo<T[]> arrayType;
  private final ClassInfo<?> componentType;

  private AnnotationType<? extends Annotation> annotationType;

  /**对int的类型标识，泛型引用封装数据类型，本身引用仍为基本数据类型*/
  public static final ClassInfo<Integer> INT_TYPE = new ClassInfo<>(int.class);

  /**对float的类型标识，泛型引用封装数据类型，本身引用仍为基本数据类型*/
  public static final ClassInfo<Float> FLOAT_TYPE = new ClassInfo<>(float.class);

  /**对boolean的类型标识，泛型引用封装数据类型，本身引用仍为基本数据类型*/
  public static final ClassInfo<Boolean> BOOLEAN_TYPE = new ClassInfo<>(boolean.class);

  /**对byte的类型标识，泛型引用封装数据类型，本身引用仍为基本数据类型*/
  public static final ClassInfo<Byte> BYTE_TYPE = new ClassInfo<>(byte.class);

  /**对short的类型标识，泛型引用封装数据类型，本身引用仍为基本数据类型*/
  public static final ClassInfo<Short> SHORT_TYPE = new ClassInfo<>(short.class);

  /**对long的类型标识，泛型引用封装数据类型，本身引用仍为基本数据类型*/
  public static final ClassInfo<Long> LONG_TYPE = new ClassInfo<>(long.class);

  /**对double的类型标识，泛型引用封装数据类型，本身引用仍为基本数据类型*/
  public static final ClassInfo<Double> DOUBLE_TYPE = new ClassInfo<>(double.class);

  /**对char的类型标识，泛型引用封装数据类型，本身引用仍为基本数据类型*/
  public static final ClassInfo<Character> CHAR_TYPE = new ClassInfo<>(char.class);

  /**对void的类型标识，泛型引用封装数据类型，本身引用仍为基本数据类型*/
  public static final ClassInfo<Void> VOID_TYPE = new ClassInfo<>(void.class);

  /**对{@link Object}的类型标识*/
  public static final ClassInfo<Object> OBJECT_TYPE = new ClassInfo<>(Object.class);

  /**对{@link String}的类型标识*/
  public static final ClassInfo<String> STRING_TYPE = asType(String.class);

  /**对{@link Class}的类型标识*/
  @SuppressWarnings("rawtypes")
  public static final ClassInfo<Class> CLASS_TYPE = asType(Class.class);

  boolean initialized;

  final boolean isPrimitive;

  /**创建一个类型标识用于标记类型，若这个目标类型已经被标记过则会返回那个已有对象标识
   *
   * @param clazz 要用于标记的类对象*/
  @SuppressWarnings("unchecked")
  public static <T> ClassInfo<T> asType(Class<T> clazz){
    ClassInfo<T> res = (ClassInfo<T>) classMap.get(clazz);

    if(res == null){
      res = clazz.isArray()? new ClassInfo<>(asType(clazz.getComponentType())): new ClassInfo<>(
          clazz.getModifiers(),
          clazz.getName(),
          clazz.getSuperclass() == null? null: clazz.getSuperclass().equals(Object.class)? OBJECT_TYPE : asType(clazz.getSuperclass()),
          Arrays.stream(clazz.getInterfaces()).map(ClassInfo::asType).toArray(ClassInfo[]::new)
      );
      res.clazz = clazz;

      classMap.put(clazz, res);

      if(clazz.isAnnotation())
        res.asAnnotation(null);

      res.initAnnotations();
    }

    return res;
  }

  /**不应该从外部调用此方法，该方法仅用于传入java基础类型的类对象获得其类型标识，若传入的类型不是基本java类型或者{@link Object}则抛出异常
   *
   * @param primitive 被标记的基本类型对象*/
  private ClassInfo(Class<T> primitive){
    super(primitive.getName());
    if(!primitive.isPrimitive() && primitive != Object.class) throw new IllegalArgumentException(primitive + " was not a primitive class");

    interfaces = new ArrayList<>();
    elements = new ArrayList<>();

    clazz = primitive;

    if(primitive == Object.class){
      setModifiers(Modifier.PUBLIC);

      realName = OBJECTTYPEMARK;

      fieldMap = new HashMap<>();
      methodMap = new HashMap<>();

      isPrimitive = false;
    }
    else{
      fieldMap = new HashMap<>();
      methodMap = new HashMap<>();

      setModifiers(Modifier.PUBLIC|Modifier.FINAL);

      if(primitive == int.class) realName = "I";
      else if(primitive == float.class) realName = "F";
      else if(primitive == boolean.class) realName = "Z";
      else if(primitive == byte.class) realName = "B";
      else if(primitive == short.class) realName = "S";
      else if(primitive == long.class) realName = "J";
      else if(primitive == char.class) realName = "C";
      else if(primitive == double.class) realName = "D";
      else if(primitive == void.class) realName = "V";

      isPrimitive = true;
    }

    componentType = null;

    classMap.put(primitive, this);
  }

  /**构建一个生成类型标识的实例，用于动态生成类
   *
   * @param modifiers 类的修饰符flags描述位集
   * @param name 类的全限定名称
   * @param superClass 类扩展的超类，这个类应当是可继承的
   * @param interfaces 此类要扩展的接口列表
   *
   * @throws IllegalArgumentException 若扩展的超类型为final或者不可继承，或者实现的接口中存在非接口类型*/
  public ClassInfo(int modifiers, String name, ClassInfo<? super T> superClass, ClassInfo<?>... interfaces){
    super(name);
    checkModifiers(modifiers, CLASS_ACCESS_MODIFIERS);

    if(superClass == null) superClass = OBJECT_TYPE;
    if(Modifier.isFinal(superClass.modifiers()))
      throw new IllegalArgumentException(superClass + " was a final class, cannot extend a final class");

    for(ClassInfo<?> inter: interfaces){
      if(!Modifier.isInterface(inter.modifiers()))
        throw new IllegalArgumentException("cannot implement a class " + inter + ", it must be a interface");
    }

    setModifiers(modifiers);
    this.superClass = superClass;
    this.interfaces = Arrays.asList(interfaces);

    elements = new ArrayList<>();
    fieldMap = new HashMap<>();
    methodMap = new HashMap<>();

    realName = "L" + name.replace(".", "/") + ";";

    isPrimitive = false;
    componentType = null;
  }

  private ClassInfo(ClassInfo<?> comp){
    super(comp.name() + "[]");

    superClass = OBJECT_TYPE;
    elements = new ArrayList<>();
    methodMap = new HashMap<>();
    fieldMap = new HashMap<>();

    realName = "[" + comp.realName;

    isPrimitive = false;
    componentType = comp;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ClassInfo<T[]> asArray(){
    ClassInfo<T[]> res = arrayType;
    if(res == null){
      if(clazz != null){
        res = arrayType = (ClassInfo<T[]>) asType(Array.newInstance(clazz, 0).getClass());
      }
      else res = arrayType = new ClassInfo<>(this);
    }

    res.isExistedClass(); //尝试初始化数组类型

    return res;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <A extends Annotation> AnnotationType<A> asAnnotation(Map<String, Object> defaultAttributes){
    if(defaultAttributes == null) defaultAttributes = new HashMap<>();
    checkAnnotation(defaultAttributes);

    Map<String, Object> map = defaultAttributes;
    return
        annotationType != null? (AnnotationType<A>) annotationType : (AnnotationType<A>) (annotationType = new AnnotationType<A>(){
          final Map<String,Object> def = new HashMap<>(map);

          @Override
          public IClass<A> typeClass(){
            return (IClass<A>) ClassInfo.this;
          }

          @Override
          public Map<String, Object> defaultValues(){
            return def;
          }

          @Override
          public IAnnotation<A> annotateTo(AnnotatedElement element, Map<String, Object> attributes){
            IAnnotation<A> anno = new AnnotationDef(this, element, attributes);
            element.addAnnotation(anno);
            return anno;
          }
        });
  }

  @Override
  public boolean isArray(){
    return componentType != null;
  }

  @Override
  public IClass<?> componentType(){
    return componentType;
  }

  @Override
  public Class<T> getTypeClass(){
    return isExistedClass()? clazz: null;
  }

  @Override
  public boolean isAnnotation(){
    return annotationType != null;
  }

  @Override
  public String realName(){
    return realName;
  }

  public String internalName(){
    return (realName.startsWith("L")? realName.replaceFirst("L", ""): realName).replace(";", "");
  }

  /**使用给出的类生成器构建此类型标识声明的类对象，此类型标识应当是可用的，请参阅{@link ClassInfo#checkGen()}
   *
   * @param generator 用于构建类型使用的类生成器
   * @return 构建生成的类对象
   *
   * @throws IllegalHandleException 若当前类型标识的状态不可用*/
  public Class<T> generate(AbstractClassGenerator generator){
    checkGen();

    try{
      return clazz = generator.generateClass(this);
    }catch(ClassNotFoundException e){
      throw new IllegalHandleException(e);
    }
  }

  public void initAnnotations(){
    if(!initialized){
      for(Annotation annotation: clazz.getAnnotations()){
        addAnnotation(new AnnotationDef<>(annotation));
      }
      initialized = true;
    }

    for(IField<?> field: fieldMap.values()){
      field.initAnnotations();
    }

    for(IMethod<?, ?> method: methodMap.values()){
      method.initAnnotations();
    }
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public boolean isExistedClass(){
    if(clazz != null) return true;

    if(componentType != null && componentType.isExistedClass()){
      clazz = (Class<T>) Array.newInstance(componentType.clazz, 0).getClass();
      return true;
    }

    return false;
  }

  @Override
  public boolean isPrimitive(){
    return isPrimitive;
  }

  protected void initMethods(){
    for(Method method: clazz.getDeclaredMethods()){
      getMethod(
          ClassInfo.asType(method.getReturnType()),
          method.getName(),
          Arrays.stream(method.getParameterTypes()).map(ClassInfo::asType).toArray(IClass[]::new)
      );
    }
  }

  protected void initFields(){
    for(Field field: clazz.getDeclaredFields()){
      getField(
          ClassInfo.asType(field.getType()),
          field.getName()
      );
    }
  }

  public CodeBlock<Void> getClinitBlock(){
    if(clinit == null){
      clinit = declareCinit();
    }

    return clinit;
  }

  @Override
  @SuppressWarnings("unchecked")
  //utilMethods
  public <R> MethodInfo<T, R> getMethod(IClass<R> returnType, String name, IClass<?>... args){
    return (MethodInfo<T, R>) methodMap.computeIfAbsent(pack(name, args), e -> {
      if(!isExistedClass())
        throw new IllegalHandleException("this class info is not a existed type mark, you have to declare method then get it");

      Class<?>[] paramClass = new Class[args.length];
      boolean stat = true;

      for(int i = 0; i < args.length; i++){
        if((paramClass[i] = args[i].getTypeClass()) == null){
          stat = false;
          break;
        }
      }

      Method met = null;
      if(stat){
        try{
          met = clazz.getDeclaredMethod(name, paramClass);
        }catch(NoSuchMethodException ex){
          throw new IllegalHandleException(ex);
        }
      }

      MethodInfo<T, R> method = met == null? new MethodInfo<>(this, Modifier.PUBLIC, name, returnType, Parameter.trans(args)):
          new MethodInfo<>(this, met.getModifiers(), name, returnType, Parameter.asParameter(met.getParameters()));
      if(met != null) method.initAnnotations();

      return method;
    });
  }
  //utilMethods
  @Override
  @SuppressWarnings("unchecked")
  public MethodInfo<T, Void> getConstructor(IClass<?>... args){
    return (MethodInfo<T, Void>) methodMap.computeIfAbsent(pack(INIT, args), e -> {
      if(!isExistedClass())
        throw new IllegalHandleException("this class info is not a existed type mark, you have to declare method then get it");

      Class<?>[] paramClass = new Class[args.length];
      boolean stat = true;

      for(int i = 0; i < args.length; i++){
        if((paramClass[i] = args[i].getTypeClass()) == null){
          stat = false;
          break;
        }
      }

      Constructor<?> cstr = null;
      if(stat){
        try{
          cstr = clazz.getDeclaredConstructor(paramClass);
        }catch(NoSuchMethodException ex){
          throw new IllegalHandleException(ex);
        }
      }

      MethodInfo<?, ?> res = cstr == null? new MethodInfo<>(this, Modifier.PUBLIC, INIT, VOID_TYPE, Parameter.trans(args)):
          new MethodInfo<>(this, cstr.getModifiers(), INIT, VOID_TYPE, Parameter.asParameter(cstr.getParameters()));
      if(cstr != null) res.initAnnotations();

      return res;
    });
  }

  @Override
  @SuppressWarnings("unchecked")
  public <TY> FieldInfo<TY> getField(IClass<TY> type, String name){
    return (FieldInfo<TY>) fieldMap.computeIfAbsent(name, e -> {
      if(!isExistedClass())
        throw new IllegalHandleException("this class info is not a existed type mark, you have to declare field then get it");

      int flags;
      try{
        flags = clazz.getDeclaredField(name).getModifiers();
      }catch(NoSuchFieldException ex){
        throw new IllegalHandleException(ex);
      }

      FieldInfo<TY> field = new FieldInfo<>(this, flags, name, type, null);
      type.initAnnotations();
      return field;
    });
  }

  @Override
  public boolean isAssignableFrom(IClass<?> target){
    if(isExistedClass() && target.isExistedClass() && clazz.isAssignableFrom(target.getTypeClass())){
      return true;
    }

    IClass<?> ty = target;
    if(!Modifier.isInterface(modifiers())){
      while(ty != null){
        if(equals(ty)) return true;

        ty = ty.superClass();
      }
    }
    else{
      QUEUE.clear();
      EXCLUDE.clear();

      while(ty != null){
        for(IClass<?> iClass: ty.interfaces()){
          if(EXCLUDE.add(iClass)) QUEUE.addFirst(iClass);
        }
        while(!QUEUE.isEmpty()){
          IClass<?> c = QUEUE.removeFirst();
          if(equals(c)) return true;

          for(IClass<?> iClass: c.interfaces()){
            if(EXCLUDE.add(iClass)) QUEUE.addFirst(iClass);
          }
        }

        ty = ty.superClass();
      }
    }

    return false;
  }

  private static String pack(String name, IClass<?>... args){
    StringBuilder builder = new StringBuilder(name);
    builder.append("(");
    for(IClass<?> arg: args){
      builder.append(arg.realName());
      if(arg.realName().length() != 1 || !PRIMITIVE_REAL.contains(arg.realName())){
        builder.append(",");
      }
    }
    String type = builder.toString();
    if(type.endsWith(",")) type = type.substring(0, type.length() - 1);

    return type + ")";
  }

  /**声明一个<cinit>块，返回块体声明对象，若块已存在则返回已存在的块体
   *
   * @return 静态块的方法体声明对象，若块已存在，则返回其块
   *
   * @throws IllegalHandleException 若此类型声明已经生成为类或者是类型标识*/
  public CodeBlock<Void> declareCinit(){
    return clinit != null? clinit : (clinit = declareMethod(Modifier.STATIC, CINIT, VOID_TYPE));
  }

  /**声明一个构造函数，返回构造函数体声明对象
   *
   * @param modifiers 此构造函数的修饰符flags标识，不可为static
   * @param parameters 构造函数的参数类型标识
   * @return 构造函数方法体的声明对象
   *
   * @throws IllegalArgumentException 若modifiers是包含static的或者不合法
   * @throws IllegalHandleException 若此类型声明已经生成为类或者是类型标识*/
  public CodeBlock<Void> declareConstructor(int modifiers, Parameter<?>... parameters){
    if(Modifier.isStatic(modifiers))
      throw new IllegalArgumentException("constructor cannot be static");

    return declareMethod(modifiers, INIT, VOID_TYPE, parameters);
  }

  /**声明一个方法，并返回方法块的声明对象
   *
   * @param modifiers 方法的修饰符flags标识
   * @param name 方法的名称
   * @param returnType 方法的返回值类型
   * @param parameters 方法参数类型列表
   *
   * @throws IllegalArgumentException 若modifiers不合法
   * @throws IllegalHandleException 若此类型声明已经生成为类或者是类型标识*/
  @SuppressWarnings("unchecked")
  public <R> CodeBlock<R> declareMethod(int modifiers, String name, ClassInfo<R> returnType, Parameter<?>... parameters){
    checkGen();
    checkModifiers(modifiers, METHOD_ACCESS_MODIFIERS);

    if(Modifier.isAbstract(modifiers) && Modifier.isStatic(modifiers))
      throw new IllegalArgumentException("conflicted modifiers " + Modifier.toString(modifiers));

    MethodInfo<T, R> method = (MethodInfo<T, R>) methodMap.computeIfAbsent(
        pack(name, Arrays.stream(parameters).map(Parameter::getType).toArray(IClass[]::new)),
        e -> new MethodInfo<>(this, modifiers, name, returnType, parameters));
    elements.add(method);

    return method.block();
  }

  /**声明一个字段，若需要给字段赋予默认常量值，则此字段应当为static，否则你应当在此类型的构造函数中初始化对象的成员字段默认值
   * <p>分配的默认值只能是下列基本类型或由其构成的数组类型的字面常量
   * <pre>{@code
   *   Class<?>-> AnyClass
   *   Enum<?> -> AnyEnum.object
   *   String  -> "anythings"
   *   int     -> -2147483648 ~ 2147483647
   *   float   -> -3.4028235E38F ~ 3.4028235E38F
   *   boolean -> true or false
   *   byte    -> -128 ~ 127
   *   short   -> -32768 ~ 32767
   *   long    -> -9223372036854775808L ~ 9223372036854775807L
   *   double  -> -1.7976931348623157E308D ~ 1.7976931348623157E308D
   *   char    -> 'u0000' ~ 'uFFFF'
   * }</pre>
   *
   * @param modifiers 字段的修饰符flags标识
   * @param name 字段的名称
   * @param type 字段的类型
   * @param initial 字段的默认初始值，可以为空，只能按规则分配
   *
   * @throws IllegalArgumentException 若modifiers不合法或者给出的初始化常量值不合法，请参见{@link ClassInfo#checkModifiers(int, int)}
   * @throws IllegalHandleException 若此类型声明已经生成为类或者是类型标识*/
  @SuppressWarnings("unchecked")
  public <F> FieldInfo<F> declareField(int modifiers, String name, ClassInfo<F> type, F initial){
    checkGen();
    checkModifiers(modifiers, FIELD_ACCESS_MODIFIERS);
    FieldInfo<F> field = (FieldInfo<F>) fieldMap.computeIfAbsent(name, e -> new FieldInfo<>(this, modifiers, name, type, initial));
    elements.add(field);

    if(initial != null && clinit == null
        && (field.initial() instanceof Array || field.initial() instanceof Enum<?>)){
      clinit = declareCinit();
    }

    return field;
  }

  /**检查修饰符之间是否存在冲突，以及修饰符是否可用，例如public, protected和private三者只能有其一
   *
   * @param modifiers 待检查的修饰符
   * @param access 可以接收的修饰符位集
   * @throws IllegalArgumentException 若修饰符存在冲突或者存在不可接收的修饰符*/
  private static void checkModifiers(int modifiers, int access){
    if(Modifier.isPublic(modifiers) && (modifiers & (Modifier.PROTECTED | Modifier.PRIVATE)) != 0
    || Modifier.isProtected(modifiers) && (modifiers & (Modifier.PUBLIC | Modifier.PRIVATE)) != 0
    || Modifier.isPrivate(modifiers) && (modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0
    || Modifier.isAbstract(modifiers) && (modifiers & (Modifier.FINAL | Modifier.NATIVE)) != 0
    || Modifier.isInterface(modifiers) && (modifiers & Modifier.FINAL) != 0
    || Modifier.isFinal(modifiers) && Modifier.isVolatile(modifiers))
      throw new IllegalArgumentException("modifiers was conflicted， modifiers: " + Modifier.toString(modifiers));

    if((modifiers & ~access) != 0)
      throw new IllegalArgumentException("unexpected modifiers with " + Modifier.toString(modifiers));
  }

  /**检查当前类生成状态，若不处于可修改状态则抛出异常，可修改状态应满足以下条件：
   * <ul>
   *   <li><strong>此类型标识不是现有类型标识</strong>
   *   <li><strong>此类型标识尚未完成类的生成与类对象加载</strong>
   * </ul>
   *
   * @throws IllegalHandleException 若此类型标识状态不正确*/
  public void checkGen(){
    if(isExistedClass())
      throw new IllegalHandleException(this + " was a generated object or type mark, can not handle it");
  }

  private void checkAnnotation(Map<String, Object> defAttributes){
    if((isExistedClass() && !clazz.isAnnotation()))
      throw new IllegalHandleException("clazz " + this + " was not a annotation type");

    Map<String, Object> map = new HashMap<>(defAttributes);
    for(Element element: elements()){
      if(!(element instanceof IMethod<?, ?> met) || !Modifier.isAbstract(((IMethod<?, ?>) element).modifiers()))
        throw new IllegalHandleException("clazz " + this + " was not a annotation type");

      IClass<?> type = met.returnType();
      if((!isReferenceType(type) && !type.equals(STRING_TYPE))
      || (type.isArray() && (!isReferenceType(type.componentType()) && !type.componentType().equals(STRING_TYPE))))
        throw new IllegalHandleException("unsupported return type in annotation: " + met);

      Object val = map.remove(met.name());
      if(!asType(val.getClass()).equals(type))
        throw new IllegalHandleException("attribute \"" + met.name() + "\" type was " + type + ", but given default value is " + val.getClass() + ", they should be same");
    }
    if(!map.isEmpty()) throw new IllegalArgumentException("unknown default attribute declaring: " + map);
  }

  private boolean isReferenceType(IClass<?> type){
    return type.realName().startsWith("L");
  }

  @Override
  public final IClass<? super T> superClass(){
    return superClass;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<IClass<?>> interfaces(){
    return (List) interfaces;
  }

  @Override
  public List<Element> elements(){
    return elements;
  }

  @Override
  public String toString(){
    return isExistedClass()? clazz.toString(): name();
  }

  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(!(o instanceof ClassInfo<?> classInfo)) return false;
    return Objects.equals(clazz, classInfo.clazz) && realName.equals(classInfo.realName);
  }

  @Override
  public int hashCode(){
    return Objects.hash(clazz, realName);
  }

  @Override
  public boolean isType(ElementType type){
    return type == (isAnnotation()? ElementType.ANNOTATION_TYPE: ElementType.TYPE);
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annoClass){
    Class<?> clazz = getTypeClass();
    if(clazz == null)
      throw new IllegalHandleException("only get annotation object in existed type info");

    return clazz.getAnnotation(annoClass);
  }
}
