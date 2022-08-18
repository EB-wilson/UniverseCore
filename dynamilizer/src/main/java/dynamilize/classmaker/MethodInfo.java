package dynamilize.classmaker;

import dynamilize.IllegalHandleException;
import dynamilize.classmaker.code.IClass;
import dynamilize.classmaker.code.IMethod;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MethodInfo<S, R> extends AnnotatedMember implements IMethod<S, R>{
  private final CodeBlock<R> block;

  IClass<S> owner;

  IClass<R> returnType;
  List<Parameter<?>> parameter;

  boolean initialized;

  public MethodInfo(IClass<S> owner, int modifiers, String name, IClass<R> returnType, Parameter<?>... params){
    super(name);
    setModifiers(modifiers);
    this.block = (modifiers & Modifier.ABSTRACT) != 0? null: new CodeBlock<>(this);
    this.owner = owner;
    this.returnType = returnType;
    this.parameter = Arrays.asList(params);

    Arrays.stream(params).forEach(e -> e.setOwner(this));

    if(block != null) block.initParams(owner, parameter);
  }

  @Override
  public List<Parameter<?>> parameters(){
    return parameter;
  }

  @Override
  public IClass<S> owner(){
    return owner;
  }

  @Override
  public String typeDescription(){
    StringBuilder builder = new StringBuilder();
    builder.append("(");
    for(IClass<?> arg: parameter.stream().map(Parameter::getType).toArray(IClass[]::new)){
      builder.append(arg.realName());
    }

    return builder + ")" + returnType.realName();
  }

  @Override
  public IClass<R> returnType(){
    return returnType;
  }

  @Override
  public CodeBlock<R> block(){
    return block;
  }

  @Override
  public void initAnnotations(){
    if(initialized || name().equals("<clinit>")) return;

    Class<?> clazz = owner().getTypeClass();
    if(clazz == null)
      throw new IllegalHandleException("only get annotation object in existed type info");

    try{
      if(name().equals("<init>")){
        Constructor<?> cstr = clazz.getDeclaredConstructor(parameters().stream().map(e -> e.getType().getTypeClass()).toArray(Class[]::new));
        for(Annotation annotation: cstr.getAnnotations()){
          addAnnotation(new AnnotationDef<>(annotation));
        }
      }
      else{
        Method met = clazz.getDeclaredMethod(name(), parameters().stream().map(e -> e.getType().getTypeClass()).toArray(Class[]::new));
        for(Annotation annotation: met.getAnnotations()){
          addAnnotation(new AnnotationDef<>(annotation));
        }
      }
    }catch(NoSuchMethodException e){
      throw new RuntimeException(e);
    }

    initialized = true;
  }

  @Override
  public boolean isType(ElementType type){
    return type == (Objects.equals(name(), "<init>") ? ElementType.CONSTRUCTOR: ElementType.METHOD);
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annoClass){
    Class<?> clazz = owner().getTypeClass();
    if(clazz == null)
      throw new IllegalHandleException("only get annotation object in existed type info");

    try{
      return clazz.getDeclaredMethod(name(), parameters().stream().map(e -> e.getType().getTypeClass()).toArray(Class[]::new)).getAnnotation(annoClass);
    }catch(NoSuchMethodException e){
      throw new IllegalHandleException(e);
    }
  }
}
