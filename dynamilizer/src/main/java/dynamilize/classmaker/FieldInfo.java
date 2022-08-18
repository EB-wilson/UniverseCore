package dynamilize.classmaker;

import dynamilize.IllegalHandleException;
import dynamilize.classmaker.code.IClass;
import dynamilize.classmaker.code.IField;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Modifier;

public class FieldInfo<T> extends AnnotatedMember implements IField<T>{
  IClass<?> owner;
  IClass<T> type;
  Object initial;
  boolean initialized;

  public FieldInfo(ClassInfo<?> owner, int modifiers, String name, IClass<T> type, Object initial){
    super(name);
    setModifiers(modifiers);
    this.owner = owner;
    this.type = type;
    this.initial = initial;

    if(initial != null && !Modifier.isStatic(modifiers))
      throw new IllegalArgumentException("cannot initial a constant to non-static field");

    if(initial != null){
      Class<?> initType = initial.getClass();

      if(!(initial instanceof Number)
          && !(initial instanceof Boolean)
          && !(initial instanceof String)
          && !(initial instanceof Character)
          && (!initType.isArray() || (!initType.getComponentType().isPrimitive() && !initType.getComponentType().equals(String.class)))
          && !(initial instanceof Enum<?>)){
        throw new IllegalArgumentException("initial must be a primitive, String or array, Enum, if array, it type should be primitive, enum or String");
      }

    }
  }

  @Override
  public IClass<?> owner(){
    return owner;
  }

  @Override
  public IClass<T> type(){
    return type;
  }

  @Override
  public Object initial(){
    return initial;
  }

  @Override
  public void initAnnotations(){
    if(initialized) return;

    Class<?> clazz = owner().getTypeClass();
    if(clazz == null)
      throw new IllegalHandleException("only get annotation object in existed type info");

    try{
      for(Annotation annotation: clazz.getDeclaredField(name()).getAnnotations()){
        addAnnotation(new AnnotationDef<>(annotation));
      }
    }catch(NoSuchFieldException e){
      throw new IllegalHandleException(e);
    }

    initialized = true;
  }

  @Override
  public boolean isType(ElementType type){
    return type == ElementType.FIELD;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annoClass){
    Class<?> clazz = owner().getTypeClass();
    if(clazz == null)
      throw new IllegalHandleException("only get annotation object in existed type info");

    try{
      return clazz.getDeclaredField(name()).getAnnotation(annoClass);
    }catch(NoSuchFieldException e){
      throw new IllegalHandleException(e);
    }
  }
}
