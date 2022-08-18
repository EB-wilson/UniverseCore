package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface IPutField<S, T extends S> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitPutField(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.FIELDSET;
  }

  ILocal<?> inst();

  ILocal<S> source();

  IField<T> target();
}
