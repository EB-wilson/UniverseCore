package universecore.androidcore.classes;

import com.android.dx.Label;
import com.android.dx.*;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.DexFile;
import com.android.dx.rop.annotation.*;
import com.android.dx.rop.cst.*;
import dynamilize.IllegalHandleException;
import dynamilize.classmaker.AbstractClassGenerator;
import dynamilize.classmaker.ByteClassLoader;
import dynamilize.classmaker.ClassInfo;
import dynamilize.classmaker.Parameter;
import dynamilize.classmaker.code.*;
import dynamilize.classmaker.code.annotation.AnnotatedElement;
import dynamilize.classmaker.code.annotation.IAnnotation;
import universecore.util.handler.FieldHandler;
import universecore.util.handler.MethodHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dynamilize.classmaker.ClassInfo.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class DexGenerator extends AbstractClassGenerator{
  public static final String THISPOINTER = "this";
  public static final String TMP_INT = "temp_I";
  public static final String TMP_FLOAT = "temp_F";
  public static final String TMP_DOUBLE = "temp_D";
  public static final String TMP_IARRAY = "temp_I_arr";
  public static final MethodId<Array, Object> MULTI_ARRAY =
      TypeId.get(Array.class).getMethod(
          TypeId.OBJECT,
          "newInstance",
          TypeId.get(int[].class)
      );

  DexMaker maker;
  final ByteClassLoader loader;

  TypeId<?> currType;
  MethodId<?, ?> currMethod;
  Code codeBlock;

  HashMap<String, Local> localMap = new HashMap<>();
  HashMap<dynamilize.classmaker.code.Label, Label> labelMap = new HashMap<>();

  private final DefaultReadVisitor initializer = new DefaultReadVisitor(){
    @Override
    public void visitLocal(ILocal<?> local){
      localMap.put(local.name(), codeBlock.newLocal(TypeId.get(local.type().internalName())));
    }

    @Override
    public void visitLabel(IMarkLabel label){
      labelMap.put(label.label(), new Label());
    }

    @Override
    public void visitCompare(ICompare<?> compare){
      if(compare.leftNumber().type().equals(LONG_TYPE) || compare.rightNumber().type().equals(LONG_TYPE)
      || compare.leftNumber().type().equals(FLOAT_TYPE) || compare.leftNumber().type().equals(DOUBLE_TYPE)
      || compare.rightNumber().type().equals(FLOAT_TYPE) || compare.rightNumber().type().equals(DOUBLE_TYPE))
        if(!localMap.containsKey(TMP_INT)) localMap.put(TMP_INT, codeBlock.newLocal(TypeId.INT));

      if(compare.leftNumber().type().equals(DOUBLE_TYPE) || compare.rightNumber().type().equals(DOUBLE_TYPE))
        if(!localMap.containsKey(TMP_DOUBLE)) localMap.put(TMP_DOUBLE, codeBlock.newLocal(TypeId.FLOAT));
      else if(compare.leftNumber().type().equals(FLOAT_TYPE) || compare.rightNumber().type().equals(FLOAT_TYPE))
        if(!localMap.containsKey(TMP_FLOAT)) localMap.put(TMP_DOUBLE, codeBlock.newLocal(TypeId.DOUBLE));
    }

    @Override
    public void visitNewArray(INewArray<?> newArray){
      if(newArray.arrayLength().size() > 1){
        if(!localMap.containsKey(TMP_IARRAY)) localMap.put(TMP_IARRAY, codeBlock.newLocal(TypeId.get(int[].class)));
        if(!localMap.containsKey(TMP_INT)) localMap.put(TMP_INT, codeBlock.newLocal(TypeId.INT));
      }
    }
  };

  public DexGenerator(ByteClassLoader loader){
    this.loader = loader;
  }

  @Override
  public byte[] genByteCode(ClassInfo<?> classInfo){
    maker = new DexMaker();

    visitClass(classInfo);

    return maker.generate();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T> Class<T> generateClass(ClassInfo<T> classInfo){
    try{
      return (Class<T>) loader.loadClass(classInfo.name(), false);
    }catch(ClassNotFoundException e){
      maker = new DexMaker();

      visitClass(classInfo);

      loader.declareClass(classInfo.name(), maker.generate());
      try{
        return (Class<T>) loader.loadClass(classInfo.name(), false);
      }catch(ClassNotFoundException ex){
        throw new RuntimeException(ex);
      }
    }
  }

  @Override
  public void visitClass(IClass<?> clazz){
    currType = TypeId.get(clazz.internalName());

    maker.declare(
        currType,
        "",
        clazz.modifiers(),
        TypeId.get(clazz.superClass().internalName()),
        clazz
            .interfaces().stream().map(e -> TypeId.get(e.internalName())).toArray(TypeId[]::new)
    );

    super.visitClass(clazz);

    visitAnnotation(clazz);
  }

  @Override
  public void visitMethod(IMethod<?, ?> method){
    currMethod = currType.getMethod(
        TypeId.get(method.returnType().internalName()),
        method.name(),
        method.parameters().stream().map(e -> TypeId.get(e.getType().internalName())).toArray(TypeId[]::new)
    );
    codeBlock = maker.declare(currMethod, method.modifiers());

    super.visitMethod(method);

    visitAnnotation(method);
  }

  @Override
  public void visitCodeBlock(ICodeBlock<?> block){
    localMap.clear();
    labelMap.clear();

    if(Modifier.isStatic(block.modifiers())){
      localMap.put(THISPOINTER, codeBlock.getThis(TypeId.get(block.owner().owner().internalName())));
    }

    List<ILocal<?>> list = block.getParamList();
    for(int i = 0; i < list.size(); i++){
      localMap.put(list.get(i).name(), codeBlock.getParameter(i, TypeId.get(list.get(i).type().internalName())));
    }

    initializer.visitCodeBlock(block);

    if(currMethod.isConstructor() && (!(block.codes().get(0) instanceof IInvoke<?> i)
        || !i.method().name().equals("<init>"))){
      block.owner().owner().superClass().getConstructor();

      codeBlock.invokeDirect(
          TypeId.get(block.owner().owner().superClass().internalName()).getConstructor(),
          null,
          localMap.get(THISPOINTER)
      );
    }

    super.visitCodeBlock(block);

    if(block.codes().get(block.codes().size() - 1).kind() != ElementKind.RETURN){
      if(block.owner().returnType().equals(VOID_TYPE)){
        codeBlock.returnVoid();
      }
      else throw new IllegalHandleException("non-void return type method must return a value at end line");
    }
  }

  @Override
  public void visitField(IField<?> field){
    FieldId<?, ?> f = currType.getField(TypeId.get(field.type().internalName()), field.name());
    maker.declare(f, field.modifiers(), field.initial());
    visitAnnotation(field);
  }

  @Override
  public void visitInvoke(IInvoke<?> invoke){
    MethodId method = getMethod(invoke.method());
    Local<?> retTo = invoke.returnTo() == null ? null : localMap.get(invoke.returnTo().name());
    Local<?>[] args = invoke.args().stream().map(e -> localMap.get(e.name())).toArray(Local[]::new);
    Local<?> instance = localMap.get(invoke.target().name());

    if(currMethod.isConstructor() && method.isConstructor() && invoke.callSuper()){
      codeBlock.invokeDirect(method, retTo, instance, args);
    }
    else if(invoke.callSuper()){
      codeBlock.invokeSuper(method, retTo, instance, args);
    }
    else if(Modifier.isStatic(invoke.method().modifiers())){
      codeBlock.invokeStatic(method, retTo, args);
    }
    else if(Modifier.isInterface(invoke.method().owner().modifiers())){
      codeBlock.invokeInterface(method, retTo, instance, args);
    }
    else codeBlock.invokeVirtual(method, retTo, instance, args);
  }

  @Override
  public void visitGetField(IGetField<?, ?> getField){
    FieldId field = getField(getField.source());

    Local<?> res = localMap.get(getField.target().name());
    if(Modifier.isStatic(getField.source().modifiers())) codeBlock.sget(field, res);
    else codeBlock.iget(field, res, localMap.get(getField.inst().name()));
  }

  @Override
  public void visitPutField(IPutField<?, ?> putField){
    FieldId field = getField(putField.target());

    Local<?> src = localMap.get(putField.source().name());
    if(Modifier.isStatic(putField.source().modifiers())) codeBlock.sput(field, src);
    else codeBlock.iput(field, localMap.get(putField.inst().name()), src);
  }

  @Override
  public void visitLocalSet(ILocalAssign<?, ?> localSet){
    codeBlock.move(
        localMap.get(localSet.target().name()),
        localMap.get(localSet.source().name())
    );
  }

  @Override
  public void visitOperate(IOperate<?> operate){
    BinaryOp opc = switch(operate.opCode()){
      case ADD -> BinaryOp.ADD;
      case SUBSTRUCTION -> BinaryOp.SUBTRACT;
      case MULTI -> BinaryOp.MULTIPLY;
      case DIVISION -> BinaryOp.DIVIDE;
      case REMAINING -> BinaryOp.REMAINDER;
      case LEFTMOVE -> BinaryOp.SHIFT_LEFT;
      case RIGHTMOVE -> BinaryOp.SHIFT_RIGHT;
      case UNSIGNMOVE -> BinaryOp.UNSIGNED_SHIFT_RIGHT;
      case BITSAME -> BinaryOp.AND;
      case BITOR -> BinaryOp.OR;
      case BITXOR -> BinaryOp.XOR;
    };

    codeBlock.op(
        opc,
        localMap.get(operate.resultTo().name()),
        localMap.get(operate.leftOpNumber().name()),
        localMap.get(operate.rightOpNumber().name())
    );
  }

  @Override
  public void visitCast(ICast cast){
    codeBlock.cast(
        localMap.get(cast.target().name()),
        localMap.get(cast.source().name())
    );
  }

  @Override
  public void visitGoto(IGoto iGoto){
    codeBlock.jump(labelMap.get(iGoto.target()));
  }

  @Override
  public void visitLabel(IMarkLabel label){
    codeBlock.mark(labelMap.get(label.label()));
  }

  @Override
  public void visitCompare(ICompare<?> compare){
    Comparison opc = switch(compare.comparison()){
      case EQUAL -> Comparison.EQ;
      case UNEQUAL -> Comparison.NE;
      case MORE -> Comparison.GT;
      case LESS -> Comparison.LT;
      case MOREOREQUAL -> Comparison.GE;
      case LESSOREQUAL -> Comparison.LE;
    };

    if(compare.leftNumber().type().equals(FLOAT_TYPE) || compare.leftNumber().type().equals(DOUBLE_TYPE)
    || compare.rightNumber().type().equals(FLOAT_TYPE) || compare.rightNumber().type().equals(DOUBLE_TYPE)){
      Local localA, localB;
      if(compare.leftNumber().type().equals(FLOAT_TYPE) || compare.leftNumber().type().equals(DOUBLE_TYPE)){
        localA = localMap.get(compare.leftNumber().name());
      }
      else{
        codeBlock.cast(
            localA = localMap.get(compare.rightNumber().type().equals(FLOAT_TYPE)? TMP_FLOAT : TMP_DOUBLE),
            localMap.get(compare.leftNumber().name())
        );
      }

      if(compare.rightNumber().type().equals(FLOAT_TYPE) || compare.rightNumber().type().equals(DOUBLE_TYPE)){
        localB = localMap.get(compare.rightNumber().name());
      }
      else{
        codeBlock.cast(
            localB = localMap.get(compare.leftNumber().type().equals(FLOAT_TYPE)? TMP_FLOAT : TMP_DOUBLE),
            localMap.get(compare.rightNumber().name())
        );
      }

      codeBlock.compareFloatingPoint(
          localMap.get(TMP_INT),
          localA,
          localB,
          compare.comparison() == ICompare.Comparison.EQUAL ||
              compare.comparison() == ICompare.Comparison.LESSOREQUAL ||
              compare.comparison() == ICompare.Comparison.LESS? Integer.MAX_VALUE:
              compare.comparison() == ICompare.Comparison.UNEQUAL? 0: Integer.MIN_VALUE
      );
    }
    else if(compare.leftNumber().type().equals(LONG_TYPE) && compare.rightNumber().type().equals(LONG_TYPE)){
      codeBlock.compareLongs(
          localMap.get(TMP_INT),
          localMap.get(compare.leftNumber().name()),
          localMap.get(compare.rightNumber().name())
      );
    }
    else{
      codeBlock.compare(
          opc,
          labelMap.get(compare.ifJump()),
          localMap.get(compare.leftNumber().name()),
          localMap.get(compare.rightNumber().name())
      );

      return;
    }

    codeBlock.compareZ(opc, labelMap.get(compare.ifJump()), localMap.get(TMP_INT));
  }

  @Override
  public void visitReturn(IReturn<?> iReturn){
    if(currMethod.getReturnType().equals(TypeId.VOID)) codeBlock.returnVoid();
    else codeBlock.returnValue(localMap.get(iReturn.returnValue().name()));
  }

  @Override
  public void visitInstanceOf(IInstanceOf instanceOf){
    codeBlock.instanceOfType(
        localMap.get(instanceOf.result().name()),
        localMap.get(instanceOf.target().name()),
        TypeId.get(instanceOf.type().internalName())
    );
  }

  @Override
  public void visitNewInstance(INewInstance<?> newInstance){
    codeBlock.newInstance(
        localMap.get(newInstance.instanceTo().name()),
        getMethod(newInstance.constructor()),
        newInstance.params().stream().map(e -> localMap.get(e.name())).toArray(Local[]::new)
    );
  }

  @Override
  public void visitOddOperate(IOddOperate<?> operate){
    UnaryOp opc = switch(operate.opCode()){
      case NEGATIVE -> UnaryOp.NEGATE;
      case BITNOR -> UnaryOp.NOT;
    };

    codeBlock.op(
        opc,
        localMap.get(operate.resultTo().name()),
        localMap.get(operate.operateNumber().name())
    );
  }

  @Override
  public void visitConstant(ILoadConstant<?> loadConstant){
    codeBlock.loadConstant(
        localMap.get(loadConstant.constTo().name()),
        loadConstant.constant()
    );
  }

  @Override
  public void visitNewArray(INewArray<?> newArray){
    List<ILocal<Integer>> length = newArray.arrayLength();

    if(length.size() ==1){
      codeBlock.newArray(
          localMap.get(newArray.resultTo().name()),
          localMap.get(length.get(0).name())
      );
    }
    else{
      codeBlock.loadConstant(
          localMap.get(TMP_INT),
          localMap.size()
      );

      codeBlock.newArray(
          localMap.get(TMP_IARRAY),
          localMap.get(TMP_INT)
      );

      for(int i = 0; i < newArray.arrayLength().size(); i++){
        codeBlock.loadConstant(localMap.get(TMP_INT), i);
        codeBlock.aput(
            localMap.get(TMP_IARRAY),
            localMap.get(TMP_INT),
            localMap.get(length.get(i).name())
        );
      }

      codeBlock.invokeStatic(
          MULTI_ARRAY,
          localMap.get(newArray.resultTo().name()),
          localMap.get(TMP_IARRAY)
      );
    }
  }

  @Override
  public void visitCondition(ICondition condition){
    Comparison opc = switch(condition.condCode()){
      case EQUAL -> Comparison.EQ;
      case UNEQUAL -> Comparison.NE;
      case MORE -> Comparison.GT;
      case LESS -> Comparison.LT;
      case MOREOREQUAL -> Comparison.GE;
      case LESSOREQUAL -> Comparison.LE;
    };

    codeBlock.compareZ(
        opc,
        labelMap.get(condition.ifJump()),
        localMap.get(condition.condition().name())
    );
  }

  @Override
  public void visitArrayGet(IArrayGet<?> arrayGet){
    codeBlock.aget(
        localMap.get(arrayGet.getTo().name()),
        localMap.get(arrayGet.array().name()),
        localMap.get(arrayGet.index().name())
    );
  }

  @Override
  public void visitArrayPut(IArrayPut<?> arrayPut){
    codeBlock.aput(
        localMap.get(arrayPut.array().name()),
        localMap.get(arrayPut.index().name()),
        localMap.get(arrayPut.value().name())
    );
  }

  private void visitAnnotation(AnnotatedElement element){
    TypeId<?> type = TypeId.get((Objects.requireNonNull(element instanceof IClass<?> c ? c :
        element instanceof IField<?> f ? f.owner() :
        element instanceof IMethod<?, ?> m ? m.owner() :
        element instanceof Parameter<?> p ? p.owner().owner() : null, "unknown element type")).internalName());

    Annotations annotations = getAnnotations(element.getAnnotations().toArray(IAnnotation[]::new));

    DexFile dexFile = MethodHandler.invokeDefault(maker, "getDexFile");
    ClassDefItem classDefItem = MethodHandler.invokeDefault(
        MethodHandler.<Object, Object>invokeDefault(maker, "getTypeDeclaration", type), "toClassDefItem()"
    );
    if((element.isType(ElementType.TYPE) || element.isType(ElementType.ANNOTATION_TYPE)) && element instanceof IClass<?>){
      classDefItem.setClassAnnotations(annotations, dexFile);
    }
    else if((element.isType(ElementType.CONSTRUCTOR) || element.isType(ElementType.METHOD)) && element instanceof IMethod<?, ?> m){
      CstMethodRef cstMethodRef = FieldHandler.getValueDefault(getMethod(m), "constant");
      classDefItem.addMethodAnnotations(cstMethodRef, annotations, dexFile);

      AnnotationsList annoList = new AnnotationsList(m.parameters().size());
      List<Parameter<?>> list = m.parameters();
      for(int i = 0; i < list.size(); i++){
        Annotations anno = getAnnotations(list.get(i).getAnnotations().toArray(IAnnotation[]::new));
        annoList.set(i, anno);
      }
      classDefItem.addParameterAnnotations(cstMethodRef, annoList, dexFile);
    }
    else if(element.isType(ElementType.FIELD) && element instanceof IField<?> f){
      CstFieldRef cstFieldRef = FieldHandler.getValueDefault(getField(f), "constant");
      classDefItem.addFieldAnnotations(cstFieldRef, annotations, dexFile);
    }
  }

  private Annotations getAnnotations(IAnnotation<?>... annotation){
    Annotations annotations = new Annotations();

    for(IAnnotation<?> iAnnotation: annotation){
      IClass<?> annoType = iAnnotation.annotationType().typeClass();
      Retention retention = annoType.getAnnotation(Retention.class);
      if(retention != null && retention.value() == RetentionPolicy.SOURCE) continue;

      CstType cstType = CstType.intern(com.android.dx.rop.type.Type.internReturnType(iAnnotation.annotationType().typeClass().internalName()));

      Annotation annoRop = new Annotation(cstType, AnnotationVisibility.RUNTIME);

      annotations.add(annoRop);
      for(Map.Entry<String, Object> entry: iAnnotation.pairs().entrySet()){
        annoRop.add(new NameValuePair(
            new CstString(entry.getKey()),
            MethodHandler.invokeDefault(Constant.class, "getConstant", entry.getValue())
        ));
      }
    }
    return annotations;
  }

  private FieldId getField(IField<?> field){
    return currType.getField(
        TypeId.get(field.type().internalName()),
        field.name()
    );
  }

  private MethodId getMethod(IMethod<?, ?> method){
    return currType.getMethod(
        TypeId.get(method.returnType().internalName()),
        method.name(),
        method.parameters().stream().map(e -> TypeId.get(e.getType().internalName())).toArray(TypeId[]::new)
    );
  }
}
