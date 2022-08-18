package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface IMarkLabel extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitLabel(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.MARKLABEL;
  }

  Label label();
}
