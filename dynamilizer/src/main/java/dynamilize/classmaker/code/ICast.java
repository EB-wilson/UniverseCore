package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface ICast extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitCast(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.CAST;
  }

  ILocal<?> source();

  ILocal<?> target();
}
