package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface IArrayPut<T> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitArrayPut(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.ARRAYPUT;
  }

  ILocal<T[]> array();

  ILocal<Integer> index();

  ILocal<T> value();
}
