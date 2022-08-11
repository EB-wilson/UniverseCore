package universecore.androidcore.classes;

import com.android.dx.dex.file.DexFile;
import com.android.dx.rop.code.*;
import com.android.dx.rop.cst.CstMethodRef;
import com.android.dx.rop.cst.CstNat;
import com.android.dx.rop.cst.CstString;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.type.Prototype;
import com.android.dx.rop.type.StdTypeList;
import com.android.dx.rop.type.Type;
import dynamilize.classmaker.AbstractClassGenerator;
import dynamilize.classmaker.ByteClassLoader;
import dynamilize.classmaker.ClassInfo;
import dynamilize.classmaker.code.*;
import universecore.androidcore.classes.dexmaker.BlockHead;
import universecore.androidcore.classes.dexmaker.DexClassInfo;
import universecore.androidcore.classes.dexmaker.DexFieldInfo;
import universecore.androidcore.classes.dexmaker.DexMethodInfo;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

public class DexGeneratorCoding extends AbstractClassGenerator{
  private final ByteClassLoader loader;

  private final HashMap<String, RegisterSpec> localMap = new HashMap<>();
  private final ArrayList<RegisterSpec> localList = new ArrayList<>();
  private final LinkedList<RegisterSpec> tmpDoubleRegister = new LinkedList<>();
  private final LinkedList<RegisterSpec> tmpIntLikeRegister = new LinkedList<>();
  private final LinkedList<RegisterSpec> tmpRefRegister = new LinkedList<>();
  private SourcePosition position;
  private int regOff;

  DexFile dexFile;
  DexClassInfo currClass;
  DexFieldInfo currField;
  DexMethodInfo currMethod;

  IClass<?> generating;

  BlockHead currBlock;

  public DexGeneratorCoding(ByteClassLoader classLoader){
    this.loader = classLoader;
  }

  @Override
  public byte[] genByteCode(ClassInfo<?> classInfo){
    dexFile.add(currClass.toItem());
    try{
      return dexFile.toDex(null, false);
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T> Class<T> generateClass(ClassInfo<T> classInfo){
    try{
      return (Class<T>) loader.loadClass(classInfo.name(), false);
    }catch(ClassNotFoundException e){
      loader.declareClass(classInfo.name(), genByteCode(classInfo));
      try{
        return (Class<T>) loader.loadClass(classInfo.name(), false);
      }catch(ClassNotFoundException ex){
        throw new RuntimeException(ex);
      }
    }
  }

  @Override
  public void visitClass(IClass<?> clazz){
    generating = clazz;
    currClass = new DexClassInfo(clazz);
    position = new SourcePosition(null, -1, -1);

    super.visitClass(clazz);
  }

  @Override
  public void visitMethod(IMethod<?, ?> method){
    currClass.addMethod(currMethod = new DexMethodInfo(method));
    currBlock = new BlockHead();
    currMethod.addBlock(currBlock);

    super.visitMethod(method);
  }

  @Override
  public void visitLocal(ILocal<?> local){
    Type type = Type.intern(local.type().internalName());
    RegisterSpec spec;
    localMap.put(local.name(), spec = RegisterSpec.make(regOff, type));
    localList.add(spec);
    if(type.getCategory() == 2) localList.add(spec);
    regOff += type.getCategory();

    super.visitLocal(local);
  }

  @Override
  public void visitCodeBlock(ICodeBlock<?> block){
    super.visitCodeBlock(block);
  }

  @Override
  public void visitField(IField<?> field){
    currClass.addField(currField = new DexFieldInfo(field));
  }

  @Override
  public void visitInvoke(IInvoke<?> invoke){
    Rop opcode;
    Prototype meth = Prototype.intern(invoke.method().typeDescription());

    if(Modifier.isStatic(invoke.method().modifiers())){
      opcode = Rops.opInvokeStatic(meth);
    }
    else if(invoke.method().name().equals("<init>") || Modifier.isPrivate(invoke.method().modifiers())){
      opcode = Rops.opInvokeDirect(meth);
    }
    else if(invoke.callSuper()){
      opcode = Rops.opInvokeSuper(meth);
    }
    else if(Modifier.isInterface(invoke.method().owner().modifiers())){
      opcode = Rops.opInvokeInterface(meth);
    }
    else opcode = Rops.opInvokeVirtual(meth);

    List<RegisterSpec> args = new ArrayList<>();
    if(!Modifier.isStatic(invoke.method().modifiers())) args.add(localMap.get(invoke.target().name()));
    args.addAll(Arrays.asList(invoke.args().stream().map(e -> localMap.get(e.name())).toArray(RegisterSpec[]::new)));

    if(args.size() >= 5){
      int tempOff = 0;

      for(int i = 0; i < args.size(); i++){
        currBlock.addInsn(new PlainInsn(
            Rops.opMove(localList.get(tempOff).getType()),
            position,
            RegisterSpec.make(localList.size() + tempOff, localList.get(tempOff).getType()),
            makeRegList(localList.get(tempOff))
        ));
      }

    }

    currBlock.addInsn(new ThrowingCstInsn(
        opcode,
        position,
        makeRegList(args.toArray(new RegisterSpec[0])),
        new StdTypeList(0),
        new CstMethodRef(
            new CstType(Type.intern(invoke.method().owner().realName())),
            new CstNat(
                new CstString(invoke.method().name()),
                new CstString(invoke.method().typeDescription())
            )
        )
    ));
  }

  private RegisterSpecList makeRegList(RegisterSpec... regs){
    RegisterSpecList list = new RegisterSpecList(regs.length);
    for(int i = 0; i < regs.length; i++){
      list.set(i, regs[i]);
    }
    return list;
  }

  @Override
  public void visitGetField(IGetField<?, ?> getField){

  }

  @Override
  public void visitPutField(IPutField<?, ?> putField){

  }

  @Override
  public void visitLocalSet(ILocalAssign<?, ?> localSet){

  }

  @Override
  public void visitOperate(IOperate<?> operate){

  }

  @Override
  public void visitCast(ICast cast){

  }

  @Override
  public void visitGoto(IGoto iGoto){

  }

  @Override
  public void visitLabel(IMarkLabel label){

  }

  @Override
  public void visitCompare(ICompare<?> compare){

  }

  @Override
  public void visitReturn(IReturn<?> iReturn){

  }

  @Override
  public void visitInstanceOf(IInstanceOf instanceOf){

  }

  @Override
  public void visitNewInstance(INewInstance<?> newInstance){

  }

  @Override
  public void visitOddOperate(IOddOperate<?> operate){

  }

  @Override
  public void visitConstant(ILoadConstant<?> loadConstant){

  }

  @Override
  public void visitNewArray(INewArray<?> newArray){

  }

  @Override
  public void visitCondition(ICondition condition){

  }

  @Override
  public void visitArrayGet(IArrayGet<?> arrayGet){

  }

  @Override
  public void visitArrayPut(IArrayPut<?> arrayPut){

  }

  @Override
  public void visitSwitch(ISwitch<?> zwitch){

  }

  @Override
  public void visitThrow(IThrow<?> thr){

  }
}
