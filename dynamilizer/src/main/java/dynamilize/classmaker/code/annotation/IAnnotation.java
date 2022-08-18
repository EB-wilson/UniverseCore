package dynamilize.classmaker.code.annotation;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface IAnnotation<A extends Annotation>{
  AnnotationType<A> annotationType();

  Map<String, Object> pairs();

  /**仅在已有类型标识上可用*/
  A asAnnotation();

  <T> T getAttr(String attr);
}
