package dynamilize;

/**变量计算器，函数式接口，用于对变量的当前值进行一系列处理后提供回调
 *
 * @author EBwilson */
@FunctionalInterface
public interface Calculator<Type>{
  Type calculate(Type input);
}
