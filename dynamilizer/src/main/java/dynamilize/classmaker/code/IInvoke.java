package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

import java.util.List;

public interface IInvoke<R> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitInvoke(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.INVOKE;
  }

  ILocal<?> target();

  IMethod<?, R> method();

  List<ILocal<?>> args();

  ILocal<? extends R> returnTo();

  boolean callSuper();
}
