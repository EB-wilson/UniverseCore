package dynamilize;

@FunctionalInterface
public interface Function<S, R>{
  R invoke(DynamicObject<S> self, ArgumentList args);

  default R invoke(DynamicObject<S> self, Object... args){
    ArgumentList lis = ArgumentList.as(args);
    R r = invoke(self, lis);
    lis.type().recycle();
    lis.recycle();
    return r;
  }

  default R invoke(DynamicObject<S> self, FunctionType type, Object... args){
    ArgumentList lis = ArgumentList.asWithType(type, args);
    R r = invoke(self, lis);
    lis.recycle();
    return r;
  }

  interface NonRetFunction<S>{
    void invoke(DynamicObject<S> self, ArgumentList args);
  }

  interface SuperGetFunction<S, R>{
    R invoke(DynamicObject<S> self, DataPool.ReadOnlyPool superPointer, ArgumentList args);
  }

  interface NonRetSuperGetFunc<S>{
    void invoke(DynamicObject<S> self, DataPool.ReadOnlyPool superPointer, ArgumentList args);
  }

}
