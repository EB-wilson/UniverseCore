package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface ILocalAssign<S, T extends S> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitLocalSet(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.LOCALASSIGN;
  }

  ILocal<S> source();

  ILocal<T> target();
}
