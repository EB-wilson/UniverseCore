package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

import java.util.List;

public interface INewInstance<T> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitNewInstance(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.NEWINSTANCE;
  }

  IMethod<T, Void> constructor();

  IClass<T> type();

  ILocal<? extends T> instanceTo();

  List<ILocal<?>> params();
}
