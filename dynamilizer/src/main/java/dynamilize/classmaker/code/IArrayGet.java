package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface IArrayGet<T> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitArrayGet(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.ARRAYGET;
  }

  ILocal<T[]> array();

  ILocal<Integer> index();

  ILocal<T> getTo();
}
