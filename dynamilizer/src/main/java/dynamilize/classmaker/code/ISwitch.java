package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

import java.util.Map;

public interface ISwitch<T> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitSwitch(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.SWITCH;
  }

  boolean isTable();

  Label end();

  ILocal<T> target();

  Map<T, Label> cases();

  void addCase(T caseKey, Label caseJump);
}
