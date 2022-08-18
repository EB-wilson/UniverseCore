package dynamilize;

public class FunctionEntry<S, R> implements IFunctionEntry{
  private final String name;
  private final boolean modifiable;
  private final Function<S, R> func;
  private final FunctionType type;
  private final DataPool owner;

  public FunctionEntry(String name, boolean modifiable, Function<S, R> func, FunctionType type, DataPool owner){
    this.name = name;
    this.modifiable = modifiable;
    this.func = func;
    this.type = type;
    this.owner = owner;
  }

  public FunctionEntry(String name, boolean modifiable, Function.SuperGetFunction<S, R> func, FunctionType type, DataPool owner){
    this(
        name,
        modifiable,
        (s, a) -> {
          DataPool.ReadOnlyPool p;
          R res = func.invoke(s, p = owner.getSuper(s, s.baseSuperPointer()), a);
          p.recycle();
          return res;
        },
        type,
        owner
    );
  }

  @Override
  public String getName(){
    return name;
  }

  @Override
  public boolean modifiable(){
    return modifiable;
  }

  @Override
  public DataPool owner(){
    return owner;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Function<S, R> getFunction(){
    return func;
  }

  @Override
  public FunctionType getType(){
    return type;
  }
}
