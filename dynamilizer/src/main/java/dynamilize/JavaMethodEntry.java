package dynamilize;

import dynamilize.annotation.Super;
import dynamilize.annotation.This;
import universecore.ImpCore;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

/**对{@linkplain DynamicClass#visitClass(Class) 行为样版}中方法描述的入口，在动态类中描述子实例的某一函数行为。
 * <p>方法入口的运行实际上是对样版方法的引用，因此需要确保样版方法所在的类始终有效，方法入口会生成这个方法的入口函数提供给动态对象使用
 *
 * @author EBwilson */
@SuppressWarnings("unchecked")
public class JavaMethodEntry implements IFunctionEntry{
  private final String name;
  private final FunctionType type;
  private final Function<?, ?> defFunc;

  private final boolean isFinal;
  private final DataPool owner;

  /**直接通过目标方法创建方法入口，并生成对方法引用的句柄提供给匿名函数以描述此函数行为
   *
   * @param invokeMethod 样版方法*/
  public JavaMethodEntry(Method invokeMethod, DataPool owner){
    this.name = invokeMethod.getName();
    this.isFinal = Modifier.isFinal(invokeMethod.getModifiers());
    this.owner = owner;

    if(!Modifier.isStatic(invokeMethod.getModifiers()))
      throw new IllegalHandleException("cannot assign a non-static method to function");

    Parameter[] parameters = invokeMethod.getParameters();
    ArrayList<Parameter> arg = new ArrayList<>();

    boolean thisPointer = false, superPointer = false;
    for(int i = 0; i < parameters.length; i++){
      Parameter param = parameters[i];
      if(param.getAnnotation(This.class) != null){
        if(thisPointer)
          throw new IllegalHandleException("only one self-pointer can exist");
        if(i != 0)
          throw new IllegalHandleException("self-pointer must be the first in parameters");

        thisPointer = true;
      }
      else if(param.getAnnotation(Super.class) != null){
        if(superPointer)
          throw new IllegalHandleException("only one super-pointer can exist");
        if(i != (thisPointer? 1: 0))
          throw new IllegalHandleException("super-pointer must be the first in parameters or the next of self-pointer(if self pointer was existed)");

        superPointer = true;
      }
      else arg.add(param);
    }

    type = FunctionType.inst(FunctionType.toTypes(arg));

    boolean thisP = thisPointer;
    boolean superP = superPointer;

    int offset = thisP? superP? 2: 1: 0;

    defFunc = (self, args) -> {
      Object[] argsArray = args.args();
      Object[] realArgArr = ArgumentList.getList(argsArray.length + offset);

      if(thisP) realArgArr[0] = self;

      DataPool.ReadOnlyPool superPool = null;
      if(thisP && superP) realArgArr[1] = owner.getSuper(self, superPool = self.baseSuperPointer());
      else if(!thisP && superP) realArgArr[0] = owner.getSuper(self, superPool = self.baseSuperPointer());

      if(argsArray.length != 0) System.arraycopy(argsArray, 0, realArgArr, offset, realArgArr.length);

      Object res = ImpCore.methodInvokeHelper.invoke(null, invokeMethod.getName(), realArgArr);
      ArgumentList.recycleList(realArgArr);
      if(superPool != null) superPool.recycle();
      return res;
    };
  }

  @Override
  public String getName(){
    return name;
  }

  @Override
  public boolean modifiable(){
    return !isFinal;
  }

  @Override
  public DataPool owner(){
    return owner;
  }

  @Override
  public <S, R> Function<S, R> getFunction(){
    return (Function<S, R>) defFunc;
  }

  @Override
  public FunctionType getType(){
    return type;
  }
}
