package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface IGetField<S, T extends S> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitGetField(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.FIELDGET;
  }

  ILocal<?> inst();

  IField<S> source();

  ILocal<T> target();
}
