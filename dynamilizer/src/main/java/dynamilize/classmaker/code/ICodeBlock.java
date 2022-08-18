package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

import java.util.List;

public interface ICodeBlock<R> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitCodeBlock(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.BLOCK;
  }

  IMethod<?, R> owner();

  List<Element> codes();

  List<ILocal<?>> getParamList();

  List<ILocal<?>> getParamAll();

  List<Label> labelList();

  int modifiers();
}
