package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface ILoadConstant<T> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitConstant(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.LOADCONSTANT;
  }

  T constant();

  ILocal<T> constTo();
}
