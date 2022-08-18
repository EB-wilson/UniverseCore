package dynamilize.classmaker;

import dynamilize.classmaker.code.*;

import java.util.Map;

public abstract class AbstractClassGenerator implements ElementVisitor{
  protected IClass<?> currGenerating;
  protected IField<?> currField;
  protected IMethod<?, ?> currMethod;
  protected ICodeBlock<?> currCodeBlock;

  protected Map<String, ILocal<?>> localMap;

  @Override
  public void visitClass(IClass<?> clazz){
    currGenerating = clazz;
    for(Element element: clazz.elements()){
      element.accept(this);
    }
  }

  @Override
  public void visitCodeBlock(ICodeBlock<?> block){
    currCodeBlock = block;
    for(Element element: block.codes()){
      element.accept(this);
    }
  }

  @Override
  public void visitField(IField<?> field){
    currField = field;
  }

  @Override
  public void visitMethod(IMethod<?, ?> method){
    currMethod = method;
    if(method.block() != null) method.block().accept(this);
  }

  @Override
  public void visitLocal(ILocal<?> local){
    localMap.put(local.name(), local);
  }

  public abstract byte[] genByteCode(ClassInfo<?> classInfo);

  protected abstract <T> Class<T> generateClass(ClassInfo<T> classInfo) throws ClassNotFoundException;
}
