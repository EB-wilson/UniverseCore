package dynamilize.unc;

import dynamilize.Function;
import dynamilize.FunctionType;
import dynamilize.IFunctionEntry;
import universecore.util.handler.MethodHandler;

import java.lang.reflect.Method;

public class UncJavaMethodRef implements IFunctionEntry {
  private final String name;
  private final Function<?, ?> defFunc;
  private final FunctionType type;

  public UncJavaMethodRef(Method invokeMethod){
    this.name = invokeMethod.getName();
    this.type = FunctionType.inst(invokeMethod);

    defFunc = (self, args) -> MethodHandler.invokeDefault(self.objSelf(), invokeMethod.getName(), args);
  }

  @Override
  public String getName() {
    return name;
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
