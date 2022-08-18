package dynamilize.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**排除标记，用于{@link dynamilize.DynamicClass#visitClass(Class)}访问行为样版时排除样版类中声明的成员，当方法/字段具有此注解，行为样版访问将直接忽略这个方法/字段
 *
 * @author EBwilson */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Exclude{
}
