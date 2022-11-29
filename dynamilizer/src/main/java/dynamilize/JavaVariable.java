package dynamilize;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class JavaVariable implements IVariable{
  private final JavaHandleHelper helper;
  private final Field field;

  public JavaVariable(Field field, JavaHandleHelper handler){
    this.field = field;
    this.helper = handler;
  }

  @Override
  public String name(){
    return field.getName();
  }

  @Override
  public boolean isConst(){
    return Modifier.isFinal(field.getModifiers());
  }

  @Override
  public <T> T get(DynamicObject<?> obj){
    return helper.get(field, obj);

  }

  @Override
  public void set(DynamicObject<?> obj, Object value){
    if(isConst())
      throw new IllegalHandleException("can not modifier a const variable");

    helper.set(field, obj, value);
  }
}
