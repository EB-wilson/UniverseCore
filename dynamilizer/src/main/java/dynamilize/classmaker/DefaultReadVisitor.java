package dynamilize.classmaker;

import dynamilize.classmaker.code.*;

public class DefaultReadVisitor implements ElementVisitor{
  @Override
  public void visitClass(IClass<?> clazz){
    for(Element element: clazz.elements()){
      element.accept(this);
    }
  }

  @Override
  public void visitMethod(IMethod<?, ?> method){
    method.block().accept(this);
  }

  @Override
  public void visitField(IField<?> field){}

  @Override
  public void visitLocal(ILocal<?> local){}

  @Override
  public void visitInvoke(IInvoke<?> invoke){}

  @Override
  public void visitGetField(IGetField<?, ?> getField){}

  @Override
  public void visitPutField(IPutField<?, ?> putField){}

  @Override
  public void visitLocalSet(ILocalAssign<?, ?> localSet){}

  @Override
  public void visitOperate(IOperate<?> operate){}

  @Override
  public void visitCast(ICast cast){}

  @Override
  public void visitGoto(IGoto iGoto){}

  @Override
  public void visitLabel(IMarkLabel label){}

  @Override
  public void visitCompare(ICompare<?> compare){}

  @Override
  public void visitCodeBlock(ICodeBlock<?> codeBlock){
    for(Element element: codeBlock.codes()){
      element.accept(this);
    }
  }

  @Override
  public void visitReturn(IReturn<?> iReturn){}

  @Override
  public void visitInstanceOf(IInstanceOf instanceOf){}

  @Override
  public void visitNewInstance(INewInstance<?> newInstance){}

  @Override
  public void visitOddOperate(IOddOperate<?> operate){}

  @Override
  public void visitConstant(ILoadConstant<?> loadConstant){}

  @Override
  public void visitNewArray(INewArray<?> newArray){}

  @Override
  public void visitCondition(ICondition condition){}

  @Override
  public void visitArrayGet(IArrayGet<?> arrayGet){}

  @Override
  public void visitArrayPut(IArrayPut<?> arrayPut){}

  @Override
  public void visitSwitch(ISwitch<?> zwitch){}

  @Override
  public void visitThrow(IThrow<?> thr){}
}
