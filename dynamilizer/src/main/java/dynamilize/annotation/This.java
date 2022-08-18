package dynamilize.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**this指针标记，用于{@link dynamilize.DynamicClass#visitClass(Class)}访问方法行为样版时，标记方法的第一个参数，
 * 被标记的参数在函数调用时，将作为this指针传递对象自身。此参素并不加入函数参数表匹配，请参见{@linkplain dynamilize.DynamicClass#visitClass(Class) 访问方法样版部分}
 * <p>尽管没有明确规定，但标记为this指针的参数应当是只读的（具备final修饰符）
 *
 * @author EBwilson */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface This{
}
