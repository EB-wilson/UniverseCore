package dynamilize;

public interface Func<R>{
  R invoke(ArgumentList args);

  default R invoke(Object... args){
    ArgumentList lis = ArgumentList.as(args);
    R r = invoke(lis);
    lis.type().recycle();
    lis.recycle();
    return r;
  }

  default R invoke(FunctionType type, Object... args){
    ArgumentList lis = ArgumentList.asWithType(type, args);
    R r = invoke(lis);
    lis.recycle();
    return r;
  }
}
