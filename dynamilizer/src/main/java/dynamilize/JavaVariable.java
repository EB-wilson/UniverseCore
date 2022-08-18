package dynamilize;

import universecore.ImpCore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class JavaVariable implements IVariable{
  private final Field field;

  public JavaVariable(Field field){
    this.field = field;
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
    if(Modifier.isStatic(field.getModifiers())){
      return ImpCore.fieldAccessHelper.getStatic(field.getDeclaringClass(), name());
    }
    return ImpCore.fieldAccessHelper.get(obj, name());
  }

  @Override
  public void set(DynamicObject<?> obj, Object value){
    if(isConst())
      throw new IllegalHandleException("can not modifier a const variable");

    if(Modifier.isStatic(field.getModifiers())){
      ImpCore.fieldAccessHelper.setStatic(field.getDeclaringClass(), name(), value);
    }
    else ImpCore.fieldAccessHelper.set(obj, name(), value);
  }
}
