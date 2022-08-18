package dynamilize;

public interface IFunctionEntry{
  /**
   * 获取入口的名称
   */
  String getName();

  /**
   * 此入口是否允许被替换
   */
  boolean modifiable();

  /**获取此入口所在的池*/
  DataPool owner();

  /**
   * 获取此方法入口定义的引用匿名函数
   */
  <S, R> Function<S, R> getFunction();

  /**
   * 获取此方法的形式参数表类型
   */
  FunctionType getType();
}
