package dynamilize.classmaker;

import dynamilize.IllegalHandleException;
import dynamilize.classmaker.code.IClass;
import dynamilize.classmaker.code.IMethod;
import dynamilize.classmaker.code.annotation.AnnotatedElement;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;

public class Parameter<T> extends AnnotatedMember implements AnnotatedElement{
  IClass<T> type;
  IMethod<?, ?> method;

  boolean initialized;

  public void setOwner(IMethod<?, ?> method){
    this.method = method;
  }

  public IMethod<?, ?> owner(){
    return method;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static Parameter<?>[] as(Object... infos){
    Parameter<?>[] res = new Parameter[infos.length/3];
    for(int i = 0; i < infos.length; i += 3){
      res[i/3] = new Parameter<>(
          ((Number) infos[i]).intValue(),
          infos[i + 1] instanceof IClass? (IClass) infos[i + 1] : ClassInfo.asType((Class<?>) infos[i + 1]),
          (String) infos[i + 2]
      );
    }

    return res;
  }

  public static Parameter<?>[] trans(IClass<?>... types){
    Parameter<?>[] res = new Parameter[types.length];
    for(int i = 0; i < res.length; i++){
      res[i] = new Parameter<>(0, types[i], "$param$" + i);
    }

    return res;
  }

  public static Parameter<?>[] asParameter(java.lang.reflect.Parameter... params){
    Parameter<?>[] res = new Parameter[params.length];
    for(int i = 0; i < res.length; i++){
      res[i] = new Parameter<>(params[i].getModifiers(), ClassInfo.asType(params[i].getType()), params[i].getName());
    }

    return res;
  }

  public Parameter(int modifiers, IClass<T> type, String name){
    super(name);
    setModifiers(modifiers);
    this.type = type;
  }

  public IClass<T> getType(){
    return type;
  }

  @Override
  public void initAnnotations(){
    if(initialized) return;

    Class<?> clazz = method.owner().getTypeClass();
    if(clazz == null)
      throw new IllegalHandleException("only get annotation object in existed type info");

    try{
        Method met = clazz.getDeclaredMethod(name(), method.parameters().stream().map(e -> e.getType().getTypeClass()).toArray(Class[]::new));

        java.lang.reflect.Parameter[] p = met.getParameters();
        for(int i = 0; i < p.length; i++){
          for(Annotation annotation: p[i].getAnnotations()){
            method.parameters().get(i).addAnnotation(new AnnotationDef<>(annotation));
          }
        }
    }catch(NoSuchMethodException e){
      throw new RuntimeException(e);
    }

    initialized = true;
  }

  @Override
  public boolean isType(ElementType type){
    return type == ElementType.PARAMETER;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annoClass){
    Class<?> clazz = method.owner().getTypeClass();
    if(clazz == null)
      throw new IllegalHandleException("only get annotation object in existed type info");

    try{
      for(java.lang.reflect.Parameter parameter: clazz.getDeclaredMethod(method.name(), method.parameters().stream().map(e -> e.getType().getTypeClass()).toArray(Class[]::new)).getParameters()){
        if(parameter.getName().equals(name())) return parameter.getAnnotation(annoClass);
      }
    }catch(NoSuchMethodException e){
      throw new IllegalHandleException(e);
    }

    return null;
  }
}
