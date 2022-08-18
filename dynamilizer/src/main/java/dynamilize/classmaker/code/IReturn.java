package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface IReturn<Type> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitReturn(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.RETURN;
  }

  ILocal<Type> returnValue();
}
