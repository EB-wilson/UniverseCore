package dynamilize;

public class Initializer<T>{
  private final Producer<T> init;
  private final boolean isConst;

  public Initializer(Producer<T> init){
    this(init, false);
  }

  public Initializer(Producer<T> init, boolean isConst){
    this.init = init;
    this.isConst = isConst;
  }

  public Object getInit(){
    return init.get();
  }

  public boolean isConst(){
    return isConst;
  }

  interface Producer<T>{
    T get();
  }
}
