package dynamilize.classmaker.code;

import dynamilize.classmaker.ClassInfo;
import dynamilize.classmaker.ElementVisitor;
import dynamilize.classmaker.code.annotation.AnnotatedElement;
import dynamilize.classmaker.code.annotation.AnnotationType;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public interface IClass<T> extends Element, AnnotatedElement{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitClass(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.CLASS;
  }

  /**返回此类型标识所标记的java类
   *
   * @return 此类型标记标记的类*/
  Class<T> getTypeClass();

  /**此类型标识是否为已有类型标识
   *
   * @return 若标记的类已被JVM加载*/
  boolean isExistedClass();

  boolean isPrimitive();

  IClass<T[]> asArray();

  <A extends Annotation> AnnotationType<A> asAnnotation(Map<String, Object> defaultAttributes);

  boolean isAnnotation();

  boolean isArray();

  IClass<?> componentType();

  String name();

  /**获取此类在字节码中的真实名称，例如：java.lang.Object -> Ljava/lang/Object;
   * <p>特别的，对于基本数据类型：
   * <pre>{@code
   * int     -> I
   * float   -> F
   * byte    -> B
   * short   -> S
   * long    -> J
   * double  -> D
   * char    -> C
   * boolean -> Z
   * void    -> V
   * }</pre>
   *
   * @return 类的实际名称*/
  String realName();

  /**获取此类型的字节码类型标识符，即真实名称省去首位的符号L，例如java.lang.Object -> java/lang/Object
   *
   * @return 类型的字节标识名称
   * @see ClassInfo#realName() */
  String internalName();

  int modifiers();

  IClass<? super T> superClass();

  List<IClass<?>> interfaces();

  List<Element> elements();

  <Type> IField<Type> getField(IClass<Type> type, String name);

  <R> IMethod<T, R> getMethod(IClass<R> returnType, String name, IClass<?>... args);

  IMethod<T, Void> getConstructor(IClass<?>... args);

  boolean isAssignableFrom(IClass<?> target);
}
