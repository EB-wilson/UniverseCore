package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface IInstanceOf extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitInstanceOf(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.INSTANCEOF;
  }

  ILocal<?> target();

  IClass<?> type();

  ILocal<Boolean> result();
}
