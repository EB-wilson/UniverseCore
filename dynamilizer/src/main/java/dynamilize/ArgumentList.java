package dynamilize;

import java.util.Arrays;
import java.util.Stack;

/**实参列表的封装对象，记录了一份实际参数列表，提供了一个泛型获取参数的方法，用于减少函数引用时所需的冗余类型转换。
 * <p>参数表对象为可复用对象，引用完毕后请使用{@link ArgumentList#recycle()}回收对象以减少堆内存更新频率
 *
 * @author EBwilson */
public class ArgumentList{
  public static int MAX_INSTANCE_STACK = 2048;

  public static final Stack<Object[]>[] ARG_LEN_MAP = new Stack[64];
  public static final Object[] EMP_ARG = new Object[0];

  static {
    for(int i = 1; i < ARG_LEN_MAP.length; i++){
      ARG_LEN_MAP[i] = new Stack<>();
    }
  }

  private static final Stack<ArgumentList> INSTANCES = new Stack<>();

  private Object[] args;
  private FunctionType type;

  /**私有构造器，不允许外部直接使用*/
  private ArgumentList(){}

  public static synchronized Object[] getList(int len){
    if(len == 0) return EMP_ARG;

    Stack<Object[]> stack = ARG_LEN_MAP[len];
    return stack.isEmpty()? new Object[len]: stack.pop();
  }

  public static void recycleList(Object[] list){
    if(list.length == 0) return;

    Stack<Object[]> stack = ARG_LEN_MAP[list.length];
    if(stack.size() > MAX_INSTANCE_STACK) return;
    stack.push(list);
  }

  /**使用一组实参列表获取一个封装参数列表，优先从实例堆栈中弹出，若堆栈中没有实例才会构造一个新的
   * 参数如果包含null，建议使用{@link ArgumentList#asWithType(FunctionType, Object...)}来获取参数列表，在明确指定形参的情况下执行可以具有更高的效率
   *
   * @param args 实参列表
   * @return 封装参数对象*/
  public static synchronized ArgumentList as(Object... args){
    ArgumentList res = INSTANCES.isEmpty()? new ArgumentList(): INSTANCES.pop();
    res.args = args;
    res.type = FunctionType.inst(args);

    return res;
  }

  public static synchronized ArgumentList asWithType(FunctionType type, Object... args){
    ArgumentList res = INSTANCES.isEmpty()? new ArgumentList(): INSTANCES.pop();
    res.args = args;
    res.type = type;

    return res;
  }

  /**回收实例，使实例重新入栈，若堆栈已到达最大容量，则不会继续插入实例堆栈中*/
  public void recycle(){
    args = null;
    type = null;
    if(INSTANCES.size() >= MAX_INSTANCE_STACK) return;

    INSTANCES.add(this);
  }

  /**获取给定索引处的实参值，类型根据引用推断，若类型不可用会抛出类型转换异常
   *
   * @param index 参数在列表中的索引位置
   * @param <T> 参数类型，通常可以根据引用推断
   * @return 实参的值
   * @throws ClassCastException 若接受者所需的类型与参数类型不可分配*/
  @SuppressWarnings("unchecked")
  public <T> T get(int index){
    return (T) args[index];
  }

  /**获取实参列表的数组
   *
   * @return 实参构成的数组*/
  public Object[] args(){
    return args;
  }

  /**获取形式参数类型封装对象
   *
   * @return 该实参列表的形式参数类型*/
  public FunctionType type(){
    return type;
  }

  @Override
  public String toString(){
    String arg = Arrays.toString(args);
    return "(" + arg.substring(1, arg.length() - 1) + ")";
  }
}
