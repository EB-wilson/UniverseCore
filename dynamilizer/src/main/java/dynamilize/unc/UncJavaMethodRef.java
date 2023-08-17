package dynamilize.unc;

import dynamilize.*;
import dynamilize.runtimeannos.Super;
import dynamilize.runtimeannos.This;
import universecore.ImpCore;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

public class UncJavaMethodRef implements IFunctionEntry {
  private final String name;
  private final Class<?> declared;
  private final Function<?, ?> defFunc;
  private final boolean isFinal;
  private final FunctionType type;

  public UncJavaMethodRef(Method invokeMethod, DataPool owner){
    this.name = invokeMethod.getName();
    this.declared = invokeMethod.getDeclaringClass();
    this.isFinal = Modifier.isFinal(invokeMethod.getModifiers());

    if(!Modifier.isStatic(invokeMethod.getModifiers()))
      throw new IllegalHandleException("cannot assign a non-static method to function");

    Parameter[] parameters = invokeMethod.getParameters();
    ArrayList<Class<?>> arg = new ArrayList<>();

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
      else arg.add(param.getType());
    }

    type = FunctionType.inst(arg);

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

      if(argsArray.length != 0) System.arraycopy(argsArray, 0, realArgArr, offset, argsArray.length);

      Object res = ImpCore.methodInvokeHelper.invokeStatic(declared, name, realArgArr);
      ArgumentList.recycleList(realArgArr);
      if(superPool != null) superPool.recycle();
      return res;
    };
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean modifiable() {
    return !isFinal;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <S, R> Function<S, R> getFunction() {
    return (Function<S, R>) defFunc;
  }

  @Override
  public FunctionType getType() {
    return type;
  }
}
