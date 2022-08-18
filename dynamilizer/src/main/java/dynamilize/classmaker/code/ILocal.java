package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface ILocal<T> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitLocal(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.LOCAL;
  }

  String name();

  int modifiers();

  IClass<T> type();

  Object initial();
}
