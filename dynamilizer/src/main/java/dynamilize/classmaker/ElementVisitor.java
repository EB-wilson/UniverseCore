package dynamilize.classmaker;

import dynamilize.classmaker.code.*;

public interface ElementVisitor{
  void visitClass(IClass<?> clazz);

  void visitMethod(IMethod<?, ?> method);

  void visitField(IField<?> field);

  void visitLocal(ILocal<?> local);

  void visitInvoke(IInvoke<?> invoke);

  void visitGetField(IGetField<?, ?> getField);

  void visitPutField(IPutField<?, ?> putField);

  void visitLocalSet(ILocalAssign<?, ?> localSet);

  void visitOperate(IOperate<?> operate);

  void visitCast(ICast cast);

  void visitGoto(IGoto iGoto);

  void visitLabel(IMarkLabel label);

  void visitCompare(ICompare<?> compare);

  void visitCodeBlock(ICodeBlock<?> codeBlock);

  void visitReturn(IReturn<?> iReturn);

  void visitInstanceOf(IInstanceOf instanceOf);

  void visitNewInstance(INewInstance<?> newInstance);

  void visitOddOperate(IOddOperate<?> operate);

  void visitConstant(ILoadConstant<?> loadConstant);

  void visitNewArray(INewArray<?> newArray);

  void visitCondition(ICondition condition);

  void visitArrayGet(IArrayGet<?> arrayGet);

  void visitArrayPut(IArrayPut<?> arrayPut);

  void visitSwitch(ISwitch<?> zwitch);

  void visitThrow(IThrow<?> thr);
}
