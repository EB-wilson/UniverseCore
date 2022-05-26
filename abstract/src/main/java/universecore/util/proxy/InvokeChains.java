package universecore.util.proxy;

import java.lang.reflect.Method;

public interface InvokeChains<Return, Self>{
  /**引用前一个调用链保存的方法，可以在调用链中使用，此方法会调用前一个调用链保存的ProxyHandler，并将这两个参数以及前一条访问链组成参数表传入
   * @param self 引用自身的指针
   * @param args 传入参数列表
   * @return 上一个调用链handle的返回结果*/
  Return callSuper(Self self, Object... args);

  Method superMethod();
}
