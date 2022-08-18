package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

public interface Element{
  void accept(ElementVisitor visitor);

  ElementKind kind();
}
