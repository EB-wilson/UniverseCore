package dynamilize.classmaker;

import dynamilize.IllegalHandleException;
import dynamilize.classmaker.code.IClass;
import dynamilize.classmaker.code.annotation.AnnotatedElement;
import dynamilize.classmaker.code.annotation.AnnotationType;
import dynamilize.classmaker.code.annotation.IAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AnnotationDef<A extends Annotation> implements IAnnotation<A>{
  IClass<A> typeClass;

  AnnotationType<A> annoType;
  Map<String, Object> pairs;

  A anno;

  AnnotatedElement element;

  @SuppressWarnings("unchecked")
  public AnnotationDef(A anno){
    typeClass = (IClass<A>) ClassInfo.asType(anno.annotationType());
    annoType = typeClass.asAnnotation(null);

    this.anno = anno;

    try{
      HashMap<String, Object> temp = new HashMap<>(annoType.defaultValues());
      for(Method method: anno.annotationType().getDeclaredMethods()){
        method.setAccessible(true);
        temp.put(method.getName(), method.invoke(anno));
      }
      pairs = new HashMap<>(temp);
    }catch(Throwable e){
      throw new IllegalHandleException(e);
    }
  }

  public AnnotationDef(AnnotationType<A> type, AnnotatedElement element, Map<String, Object> attributes){
    annoType = type;
    typeClass = type.typeClass();

    this.element = element;

    HashMap<String, Object> temp = new HashMap<>(annoType.defaultValues());
    if(attributes != null) temp.putAll(attributes);
    pairs = new HashMap<>(temp);
  }

  @Override
  public AnnotationType<A> annotationType(){
    return annoType;
  }

  @Override
  public Map<String, Object> pairs(){
    return pairs;
  }

  @Override
  public A asAnnotation(){
    if(anno == null){
      if(typeClass == null){
        if(!annoType.typeClass().isExistedClass())
          throw new IllegalHandleException("only get annotation object with existed type info");

        typeClass = annoType.typeClass();
      }

      anno = element.getAnnotation(typeClass.getTypeClass());
    }

    return anno;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAttr(String attr){
    return (T) pairs.computeIfAbsent(attr, e -> {throw new IllegalHandleException("no such attribute in annotation" + this);});
  }
}
