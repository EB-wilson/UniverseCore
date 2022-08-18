package dynamilize.classmaker;

import dynamilize.IllegalHandleException;
import dynamilize.classmaker.code.*;

import java.lang.reflect.Modifier;
import java.util.*;

import static dynamilize.classmaker.ClassInfo.*;
import static dynamilize.classmaker.ClassInfo.CHAR_TYPE;

public class CodeBlock<R> implements ICodeBlock<R>{
  protected ArrayList<Element> statements = new ArrayList<>();

  protected ArrayList<ILocal<?>> parameter = new ArrayList<>();
  Local<?> selfPointer;

  List<Label> labelList = new ArrayList<>();
  IMethod<?, R> method;

  public CodeBlock(IMethod<?, R> method){
    this.method = method;
  }

  protected void initParams(IClass<?> self, List<Parameter<?>> params){
    if(!Modifier.isStatic(method.modifiers())){
      selfPointer = new Local<>("this", Modifier.FINAL, self);
      parameter.add(selfPointer);
    }

    for(Parameter<?> param: params){
      parameter.add(
        new Local<>(param.name(), param.modifiers(), param.getType())
      );
    }
  }

  public IMethod<?, R> owner(){
    return method;
  }

  @Override
  public List<Element> codes(){
    return statements;
  }

  @Override
  public List<ILocal<?>> getParamList(){
    return selfPointer == null? new ArrayList<>(parameter): parameter.subList(1, parameter.size());
  }

  public List<ILocal<?>> getParamAll(){
    return new ArrayList<>(parameter);
  }

  @Override
  public int modifiers(){
    return 0;
  }

  //*=============*//
  //* utilMethods *//
  //*=============*//
  private static final String VAR_DEFAULT = "var&";

  private int defVarCount = 0;

  public <T> ILocal<T> local(IClass<T> type, String name, int flags){
    ILocal<T> res = new Local<>(name, flags, type);
    codes().add(res);
    return res;
  }

  public <T> ILocal<T> local(IClass<T> type, int flags){
    return local(type, VAR_DEFAULT + defVarCount++, flags);
  }

  public <T> ILocal<T> local(IClass<T> type){
    return local(type, VAR_DEFAULT + defVarCount++, 0);
  }

  /**获取方法的参数的局部变量，索引为此参数在参数列表中的位置，对于非静态方法，索引0号位置为this指针
   *
   * @param index 此参数在形式参数列表中的位置，非静态方法的0索引处为this指针
   * @param <T> 参数的类型*/
  @SuppressWarnings("unchecked")
  public <T> ILocal<T> getParam(int index){
    return (ILocal<T>) parameter.get(index);
  }

  /**获取方法的参数的局部变量，索引为此参数在参数列表中的位置，不同于{@link CodeBlock#getParam(int)}，索引0处是第一个形参而不是this
   *
   * @param index 此参数在形式参数列表中的位置
   * @param <T> 参数的类型*/
  @SuppressWarnings("unchecked")
  public <T> ILocal<T> getRealParam(int index){
    return (ILocal<T>) parameter.get(selfPointer != null? index + 1: index);
  }

  @Override
  public List<Label> labelList(){
    return labelList;
  }

  @SuppressWarnings("unchecked")
  public <T> ILocal<T> getThis(){
    if(selfPointer == null)
      throw new IllegalHandleException("static method no \"this\" pointer");

    return (ILocal<T>) selfPointer;
  }

  public final <S, T extends S> void assignField(ILocal<?> target, IField<S> src, IField<T> tar){
    ILocal<S> srcTemp = local(src.type());
    assign(target, src, srcTemp);
    assign(target, srcTemp, tar);
  }

  public final <S, T extends S> void assign(ILocal<S> src, ILocal<T> tar){
    codes().add(
        new LocalAssign<>(src, tar)
    );
  }

  public final <S, T extends S> void assign(ILocal<?> tar, IField<S> src, ILocal<T> to){
    codes().add(
        new GetField<>(tar, src, to)
    );
  }

  /**将目标对象的指定属性赋值为给出的局部变量的值
   *
   * @param tar 保存要设置字段的目标对象的局部变量
   * @param src 设置值的来源局部变量
   * @param to 需要被写入值的字段*/
  public final <S, T extends S> void assign(ILocal<?> tar, ILocal<S> src, IField<T> to){
    codes().add(
        new PutField<>(tar, src, to)
    );
  }

  public final <S, T extends S> void assign(ILocal<?> tar, String src, ILocal<T> to){
    codes().add(
        new GetField<>(tar, tar.type().getField(to.type(), src), to)
    );
  }

  public final <S> void assign(ILocal<?> tar, ILocal<S> src, String to){
    codes().add(
        new PutField<>(tar, src, tar.type().getField(src.type(), to))
    );
  }

  public final <S, T extends S> void assignStatic(IClass<?> clazz, String src, ILocal<T> to){
    codes().add(
        new GetField<>(null, clazz.getField(to.type(), src), to)
    );
  }

  public final <S> void assignStatic(IClass<?> clazz, ILocal<S> src, String to){
    codes().add(
        new PutField<>(null, src, clazz.getField(src.type(), to))
    );
  }

  public final <Ret> void invoke(ILocal<?> target, IMethod<?, Ret> method, ILocal<Ret> returnTo, ILocal<?>... args){
    codes().add(
        new Invoke<>(target, false, method, returnTo, args)
    );
  }

  public final <Ret> void invoke(ILocal<?> target, String method, ILocal<Ret> returnTo, ILocal<?>... args){
    codes().add(
        new Invoke<>(target, false, target.type().getMethod(returnTo.type(), method, Arrays.stream(args).map(ILocal::type).toArray(IClass<?>[]::new)), returnTo, args)
    );
  }

  public final <Ret> void invokeStatic(IClass<?> target, String method, ILocal<Ret> returnTo, ILocal<?>... args){
    codes().add(
        new Invoke<>(null, false, target.getMethod(returnTo.type(), method, Arrays.stream(args).map(ILocal::type).toArray(IClass<?>[]::new)), returnTo, args)
    );
  }

  public final <Ret> void invokeSuper(ILocal<?> target, IMethod<?, Ret> method, ILocal<Ret> returnTo, ILocal<?>... args){
    codes().add(
        new Invoke<>(target, true, method, returnTo, args)
    );
  }

  public final <Ret> void invokeSuper(ILocal<?> target, String method, ILocal<Ret> returnTo, ILocal<?>... args){
    codes().add(
        new Invoke<>(target, true, target.type().getMethod(returnTo.type(), method, Arrays.stream(args).map(ILocal::type).toArray(IClass<?>[]::new)), returnTo, args)
    );
  }

  public final <T extends R> void returnValue(ILocal<T> local){
    codes().add(
        new Return<>(local)
    );
  }

  public final void returnVoid(){
    codes().add(
        new Return<>(null)
    );
  }

  public final <T> void operate(IOddOperate.OddOperator opCode, ILocal<T> opNumb, ILocal<T> to){
    codes().add(
        new OddOperate<>(opCode, opNumb, to)
    );
  }

  public final <T> void operate(String symbol, ILocal<T> opNumb, ILocal<T> to){
    codes().add(
        new OddOperate<>(IOddOperate.OddOperator.as(symbol), opNumb, to)
    );
  }

  public final <T> void operate(ILocal<T> lefOP, IOperate.OPCode opCode, ILocal<T> rigOP, ILocal<?> to){
    codes().add(
        new Operate<>(opCode, lefOP, rigOP, to)
    );
  }

  public final <T> void operate(ILocal<T> lefOP, String symbol, ILocal<T> rigOP, ILocal<?> to){
    codes().add(
        new Operate<>(IOperate.OPCode.as(symbol), lefOP, rigOP, to)
    );
  }

  public final Label label(){
    Label l = new Label();
    labelList.add(l);

    return l;
  }

  public final void markLabel(Label label){
    codes().add(
        new MarkLabel(label)
    );
  }

  public final void jump(Label label){
    codes().add(
        new Goto(label)
    );
  }

  public final <T> void compare(ILocal<T> lef, ICompare.Comparison opc, ILocal<T> rig, Label ifJump){
    codes().add(
        new Compare<>(lef, rig, ifJump, opc)
    );
  }

  public final <T> void compare(ILocal<T> lef, String symbol, ILocal<T> rig, Label ifJump){
    compare(lef, ICompare.Comparison.as(symbol), rig, ifJump);
  }

  public final <T> void condition(ILocal<T> target, String symbol, Label ifJump){
    condition(target, ICondition.CondCode.as(symbol), ifJump);
  }

  public final <T> void condition(ILocal<T> target, ICondition.CondCode condCode, Label ifJump){
    codes().add(
        new Condition(target, condCode, ifJump)
    );
  }

  public final void cast(ILocal<?> source, ILocal<?> target){
    codes().add(
        new Cast(source, target)
    );
  }

  public final void instanceOf(ILocal<?> target, IClass<?> type, ILocal<Boolean> result){
    codes().add(
        new InstanceOf(target, type, result)
    );
  }

  public final  <T> void newInstance(IMethod<T, Void> constructor, ILocal<? extends T> resultTo, ILocal<?>... params){
    codes().add(
        new NewInstance<>(constructor, resultTo, params)
    );
  }

  @SafeVarargs
  public final <T> void newArray(IClass<T> arrElemType, ILocal<?> storeTo, ILocal<Integer>... length){
    codes().add(
        new NewArray<>(arrElemType, Arrays.asList(length), storeTo)
    );
  }

  public final <T> void arrayPut(ILocal<T[]> array, ILocal<Integer> index, ILocal<T> value){
    codes().add(
      new ArrayPut<>(array, index, value)
    );
  }

  public final <T> void arrayGet(ILocal<T[]> array, ILocal<Integer> index, ILocal<T> getTo){
    codes().add(
      new ArrayGet<>(array, index, getTo)
    );
  }

  public final <T> void loadConstant(ILocal<T> tar, T constant){
    codes().add(
        new LoadConstant<>(constant, tar)
    );
  }

  public final <T extends Comparable<?>> ISwitch<T> switchCase(ILocal<T> target, Label end, Object... casePairs){
    Switch<T> swi;
    codes().add(
        swi = new Switch<>(target, end, casePairs)
    );

    return swi;
  }

  public final <T extends Comparable<?>> ISwitch<T> switchDef(ILocal<T> target, Label end){
    Switch<T> swi;
    codes().add(
        swi = new Switch<>(target, end)
    );

    return swi;
  }

  public final <T extends Throwable> void thr(ILocal<T> throwable){
    codes().add(
        new Throw<>(throwable)
    );
  }

  //*===============*//
  //*memberCodeTypes*//
  //*===============*//
  protected static class Local<T> implements ILocal<T>{
    String name;
    int modifiers;
    IClass<T> type;

    Object initial;

    public Local(String name, int modifiers, IClass<T> type){
      this.name = name;
      this.modifiers = modifiers;
      this.type = type;
    }

    @Override
    public String name(){
      return name;
    }

    @Override
    public int modifiers(){
      return modifiers;
    }

    @Override
    public IClass<T> type(){
      return type;
    }

    @Override
    public Object initial(){
      return initial;
    }
  }

  protected static class Operate<T> implements IOperate<T>{
    OPCode opc;

    ILocal<T> leftOP, rightOP;
    ILocal<?> result;

    public Operate(OPCode opc, ILocal<T> leftOP, ILocal<T> rightOP, ILocal<?> result){
      this.opc = opc;
      this.leftOP = leftOP;
      this.rightOP = rightOP;
      this.result = result;
    }

    @Override
    public OPCode opCode(){
      return opc;
    }

    @Override
    public ILocal<?> resultTo(){
      return result;
    }

    @Override
    public ILocal<T> leftOpNumber(){
      return leftOP;
    }

    @Override
    public ILocal<T> rightOpNumber(){
      return rightOP;
    }
  }

  protected static class OddOperate<T> implements IOddOperate<T>{
    ILocal<T> opNumb;
    ILocal<T> retTo;

    OddOperator opCode;

    public OddOperate( OddOperator opCode, ILocal<T> opNumb, ILocal<T> retTo){
      this.opNumb = opNumb;
      this.retTo = retTo;
      this.opCode = opCode;
    }

    @Override
    public ILocal<T> operateNumber(){
      return opNumb;
    }

    @Override
    public ILocal<T> resultTo(){
      return retTo;
    }

    @Override
    public OddOperator opCode(){
      return opCode;
    }
  }

  protected static class LocalAssign<S, T extends S> implements ILocalAssign<S, T>{
    ILocal<S> src;
    ILocal<T> tar;

    public LocalAssign(ILocal<S> src, ILocal<T> tar){
      this.src = src;
      this.tar = tar;
    }

    @Override
    public ILocal<S> source(){
      return src;
    }

    @Override
    public ILocal<T> target(){
      return tar;
    }
  }

  protected static class PutField<S, T extends S> implements IPutField<S, T>{
    ILocal<?> inst;
    ILocal<S> source;
    IField<T> target;

    public PutField(ILocal<?> inst, ILocal<S> source, IField<T> target){
      this.inst = inst;
      this.source = source;
      this.target = target;
    }

    @Override
    public ILocal<?> inst(){
      return inst;
    }

    @Override
    public ILocal<S> source(){
      return source;
    }

    @Override
    public IField<T> target(){
      return target;
    }
  }

  protected static class GetField<S, T extends S> implements IGetField<S, T>{
    ILocal<?> inst;
    IField<S> source;
    ILocal<T> target;

    public GetField(ILocal<?> inst, IField<S> source, ILocal<T> target){
      this.inst = inst;
      this.source = source;
      this.target = target;
    }

    @Override
    public ILocal<?> inst(){
      return inst;
    }

    @Override
    public IField<S> source(){
      return source;
    }

    @Override
    public ILocal<T> target(){
      return target;
    }
  }

  protected static class Goto implements IGoto{
    Label label;

    public Goto(Label label){
      this.label = label;
    }

    @Override
    public Label target(){
      return label;
    }
  }

  protected static class Invoke<R> implements IInvoke<R>{
    IMethod<?, R> method;
    ILocal<? extends R> returnTo;
    List<ILocal<?>> args;
    ILocal<?> target;

    boolean callSuper;

    public Invoke(ILocal<?> target, boolean callSuper, IMethod<?, R> method, ILocal<? extends R> returnTo, ILocal<?>... args){
      this.method = method;
      this.returnTo = method.returnType() != ClassInfo.VOID_TYPE? returnTo: null;
      this.target = target;
      this.args = Arrays.asList(args);
      this.callSuper = callSuper;

      if(callSuper && !method.owner().isAssignableFrom(target.type()))
        throw new IllegalHandleException("cannot call super method in non-super class");
    }

    @Override
    public ILocal<?> target(){
      return target;
    }

    @Override
    public IMethod<?, R> method(){
      return method;
    }

    @Override
    public List<ILocal<?>> args(){
      return args;
    }

    @Override
    public ILocal<? extends R> returnTo(){
      return returnTo;
    }

    @Override
    public boolean callSuper(){
      return callSuper;
    }
  }

  protected static class Compare<T> implements ICompare<T>{
    ILocal<T> left;
    ILocal<T> right;

    Label jumpTo;

    Comparison comparison;

    public Compare(ILocal<T> left, ILocal<T> right, Label jumpTo, Comparison comparison){
      this.left = left;
      this.right = right;
      this.jumpTo = jumpTo;
      this.comparison = comparison;
    }

    @Override
    public ILocal<T> leftNumber(){
      return left;
    }

    @Override
    public ILocal<T> rightNumber(){
      return right;
    }

    @Override
    public Label ifJump(){
      return jumpTo;
    }

    @Override
    public Comparison comparison(){
      return comparison;
    }
  }

  protected static class Cast implements ICast{
    ILocal<?> src, tar;

    public Cast(ILocal<?> src, ILocal<?> tar){
      this.src = src;
      this.tar = tar;
    }

    @Override
    public ILocal<?> source(){
      return src;
    }

    @Override
    public ILocal<?> target(){
      return tar;
    }
  }

  protected static class Return<R> implements IReturn<R>{
    ILocal<R> local;

    public Return(ILocal<R> local){
      this.local = local;
    }

    @Override
    public ILocal<R> returnValue(){
      return local;
    }
  }

  protected static class InstanceOf implements IInstanceOf{
    ILocal<?> target;
    IClass<?> type;
    ILocal<Boolean> result;

    public InstanceOf(ILocal<?> target, IClass<?> type, ILocal<Boolean> result){
      this.target = target;
      this.type = type;
      this.result = result;
    }

    @Override
    public ILocal<?> target(){
      return null;
    }

    @Override
    public IClass<?> type(){
      return null;
    }

    @Override
    public ILocal<Boolean> result(){
      return null;
    }
  }

  protected static class NewInstance<T> implements INewInstance<T>{
    IMethod<T, Void> constructor;
    ILocal<? extends T> resultTo;

    IClass<T> type;

    List<ILocal<?>> params;

    public NewInstance(IMethod<T, Void> constructor, ILocal<? extends T> resultTo, ILocal<?>... params){
      this.constructor = constructor;
      this.type = constructor.owner();
      this.resultTo = resultTo;
      this.params = Arrays.asList(params);
    }

    @Override
    public IMethod<T, Void> constructor(){
      return constructor;
    }

    @Override
    public IClass<T> type(){
      return type;
    }

    @Override
    public ILocal<? extends T> instanceTo(){
      return resultTo;
    }

    @Override
    public List<ILocal<?>> params(){
      return params;
    }
  }

  protected static class NewArray<T> implements INewArray<T>{
    IClass<T> arrCompType;
    List<ILocal<Integer>> arrayLength;
    ILocal<?> retTo;

    public NewArray(IClass<T> arrCompType, List<ILocal<Integer>> arrayLength, ILocal<?> retTo){
      this.arrCompType = arrCompType;
      this.arrayLength = arrayLength;
      this.retTo = retTo;
    }

    @Override
    public IClass<T> arrayEleType(){
      return arrCompType;
    }

    @Override
    public List<ILocal<Integer>> arrayLength(){
      return arrayLength;
    }

    @Override
    public ILocal<?> resultTo(){
      return retTo;
    }
  }

  public static class ArrayPut<T> implements IArrayPut<T>{
    ILocal<T[]> array;
    ILocal<Integer> index;
    ILocal<T> value;

    public ArrayPut(ILocal<T[]> array, ILocal<Integer> index, ILocal<T> value){
      this.array = array;
      this.index = index;
      this.value = value;
    }

    @Override
    public ILocal<T[]> array(){
      return array;
    }

    @Override
    public ILocal<Integer> index(){
      return index;
    }

    @Override
    public ILocal<T> value(){
      return value;
    }
  }

  public static class ArrayGet<T> implements IArrayGet<T>{
    ILocal<T[]> array;
    ILocal<Integer> index;
    ILocal<T> getTo;

    public ArrayGet(ILocal<T[]> array, ILocal<Integer> index, ILocal<T> getTo){
      this.array = array;
      this.index = index;
      this.getTo = getTo;
    }

    @Override
    public ILocal<T[]> array(){
      return array;
    }

    @Override
    public ILocal<Integer> index(){
      return index;
    }

    @Override
    public ILocal<T> getTo(){
      return getTo;
    }
  }

  protected static class LoadConstant<T> implements ILoadConstant<T>{
    T constant;
    ILocal<T> resTo;

    public LoadConstant(T constant, ILocal<T> resTo){
      this.constant = constant;
      this.resTo = resTo;
    }

    @Override
    public T constant(){
      return constant;
    }

    @Override
    public ILocal<T> constTo(){
      return resTo;
    }
  }

  protected static class MarkLabel implements IMarkLabel{
    Label label;

    public MarkLabel(Label label){
      this.label = label;
    }

    @Override
    public Label label(){
      return label;
    }
  }

  protected static class Condition implements ICondition{
    ILocal<?> condition;
    CondCode condCode;
    Label ifJump;

    public Condition(ILocal<?> condition, CondCode condCode, Label ifJump){
      this.condition = condition;
      this.condCode = condCode;
      this.ifJump = ifJump;
    }

    @Override
    public CondCode condCode(){
      return condCode;
    }

    @Override
    public ILocal<?> condition(){
      return condition;
    }

    @Override
    public Label ifJump(){
      return ifJump;
    }
  }

  protected static class Switch<T> implements ISwitch<T>{
    ILocal<T> target;
    Map<T, Label> casesMap;

    Label end;

    boolean isTable;

    @SuppressWarnings("unchecked")
    public Switch(ILocal<T> target, Label end, Object... pairs){
      this.target = target;
      this.casesMap = new TreeMap<>((a, b) -> a.hashCode() - b.hashCode());

      this.end = end;

      for(int i = 0; i < pairs.length; i += 2){
        casesMap.put((T) pairs[i], (Label) pairs[i + 1]);
      }

      checkTable();
    }

    public Switch(ILocal<T> target, Label end){
      this.target = target;
      this.casesMap = new TreeMap<>();
      this.end = end;
    }

    @Override
    public boolean isTable(){
      return isTable;
    }

    @Override
    public Label end(){
      return end;
    }

    @Override
    public ILocal<T> target(){
      return target;
    }

    @Override
    public Map<T, Label> cases(){
      return casesMap;
    }

    @Override
    public void addCase(T caseKey, Label caseJump){
      casesMap.put(caseKey, caseJump);

      checkTable();
    }

    protected void checkTable(){
      if(target.type() == BOOLEAN_TYPE || (!target.type().isPrimitive()
          && !ClassInfo.asType(Enum.class).isAssignableFrom(target.type())
          && target.type() != STRING_TYPE))
        throw new IllegalHandleException("unsupported type error");

      if(target.type() == LONG_TYPE || target.type() == INT_TYPE
      || target.type() == SHORT_TYPE || target.type() == BYTE_TYPE
      || target.type() == CHAR_TYPE || target.type() instanceof Enum<?>){
        int labelsNumber = casesMap.size();

        int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
        for(T t: casesMap.keySet()){
          if(t instanceof Number n){
            max = Math.max(max, n.intValue());
            min = Math.min(min, n.intValue());
          }
          else if(t instanceof Character c){
            max = Math.max(max, c);
            min = Math.min(min, c);
          }
          else if(t instanceof Enum<?> e){
            max = Math.max(max, e.ordinal());
            min = Math.min(min, e.ordinal());
          }
        }

        int tableSpaceCost = 4 + max - min + 1;
        int tableTimeCost = 3;
        int lookupSpaceCost = 3 + 2*labelsNumber;

        isTable = labelsNumber > 0 && tableSpaceCost + 3*tableTimeCost <= lookupSpaceCost + 3*labelsNumber;
      }
      else isTable = false;
    }
  }

  protected static class Throw<T extends Throwable> implements IThrow<T>{
    ILocal<T> thr;

    public Throw(ILocal<T> thr){
      this.thr = thr;
    }

    @Override
    public ILocal<T> thr(){
      return thr;
    }
  }
}
