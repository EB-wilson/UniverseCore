package dynamilize.classmaker.code.annotation;

import dynamilize.classmaker.code.IClass;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.List;

public interface AnnotatedElement{
  List<IAnnotation<?>> getAnnotations();

  void initAnnotations();

  boolean hasAnnotation(IClass<? extends Annotation> annoType);

  <T extends Annotation> IAnnotation<T> getAnnotation(IClass<T> annoType);

  boolean isType(ElementType type);

  <A extends Annotation> A getAnnotation(Class<A> annoClass);

  void addAnnotation(IAnnotation<?> annotation);
}
