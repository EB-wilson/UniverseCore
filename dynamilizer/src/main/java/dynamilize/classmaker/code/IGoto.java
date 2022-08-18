package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface IGoto extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitGoto(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.GOTO;
  }

  Label target();
}
