package dynamilize;

public class Variable implements IVariable{
  private final String name;
  private final boolean isConst;

  public Variable(String name, boolean isConst){
    this.name = name;
    this.isConst = isConst;
  }

  @Override
  public String name(){
    return name;
  }

  @Override
  public boolean isConst(){
    return isConst;
  }

  @Override
  public <T> T get(DynamicObject<?> obj){
    return obj.varValueGet(name);
  }

  @Override
  public void set(DynamicObject<?> obj, Object value){
    obj.varValueSet(name, value);
  }
}
