package universecore.components;

import arc.func.Boolp;
import arc.func.Floatp;
import arc.func.Intp;
import arc.func.Prov;
import universecore.annotations.Annotations;
import universecore.util.funcs.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**附加变量接口，用于为类型提供动态附加变量，提供了对变量的{@code get}，{@code set}，{@code handle}等操作，但不可移除变量。
 * <br>（但是事实上你可以通过调用{@link ExtraVariableComp#extra()}方法取出变量映射表强行删除，但这是违背本类型设计初衷的）
 *
 * @apiNote 我真的恨死原始数据类型的封装了，这绝对绝对是java中最令人恶心的设计
 * @since 1.6
 * @author EBwilson */
@SuppressWarnings("unchecked")
public interface ExtraVariableComp{
  /**变量映射表入口，自动或手动绑定到一个映射对象，它将作为对象保存动态变量的容器使用
   * <br><i>不鼓励主动调用该方法直接操作变量</i>*/
  @Annotations.BindField(value = "extraVar", initialize = "new universecore.util.colletion.CollectionObjectMap<>()")
  default Map<String, Object> extra(){
    return null;
  }

  /**获取动态变量的值，如果变量不存在，则返回null
   *
   * @param <T> 获取变量的类型
   * @param field 变量名称*/
  default <T> T getVar(String field){
    return (T) extra().get(field);
  }

  /**获取动态变量的值，若变量不存在会返回给出的默认值
   * <br><strong>注意：</strong>若变量不存在，默认值直接被返回，不会被加入到变量表
   *
   * @param <T> 获取变量的类型
   * @param field 变量名称
   * @param def 默认值*/
  default <T> T getVar(String field, T def){
    return (T) extra().getOrDefault(field, def);
  }

  /**获取动态变量的值，若变量不存在，则返回给出的初始化函数的返回值，并将这个值赋值给给定的变量，这通常被用于进行便捷的变量值初始化
   *
   * @param <T> 获取变量的类型
   * @param field 变量名称
   * @param initial 初始值函数*/
  default <T> T getVar(String field, Prov<T> initial){
    return (T) extra().computeIfAbsent(field, e -> initial.get());
  }

  /**获取动态变量的值，若变量不存在则抛出异常
   *
   * @param <T> 获取变量的类型
   * @param field 变量名称
   *
   * @throws NoSuchFieldException 若获取的变量不存在*/
  default <T> T getVarThr(String field) throws NoSuchFieldException {
    if (!extra().containsKey(field))
      throw new NoSuchFieldException("no such field with name: " + field);

    return (T) extra().get(field);
  }

  /**设置指定变量的值
   *
   * @param <T> 设置变量的类型
   * @param field 变量名称
   * @param value 设置的变量值
   * @return 变量被设置前原本的值*/
  default <T> T setVar(String field, T value){
    return (T) extra().put(field, value);
  }

  /**使用一个函数处理变量的值，并使用返回值更新变量的值
   *
   * @param <T> 设置变量的类型
   * @param field 变量名称
   * @param cons 变量处理函数
   * @param def 变量默认值
   * @return 更新后的变量值，即函数的返回值*/
  default <T> T handleVar(String field, Function<T, T> cons, T def){
    T res;
    setVar(field, res = cons.apply(getVar(field, def)));

    return res;
  }

  //-----------------------
  //原始数据类型操作的优化重载
  //
  //java的原始数据类型装箱一定
  //是编程语言史上可以排进前十
  //的愚蠢行为
  //-----------------------

  /**设置boolean类型变量值
   *
   * @see ExtraVariableComp#setVar(String, Object)
   * @throws ClassCastException 如果变量已存在且不是boolean的封装类型或原子化引用*/
  default boolean setVar(String field, boolean value){
    Object res = getVar(field);

    if (res instanceof AtomicBoolean b){
      boolean r = b.get();
      b.set(value);
      return r;
    }
    else if (res instanceof Boolean n){
      extra().put(field, new AtomicBoolean(value));
      return n;
    }
    else if (res == null){
      extra().put(field, new AtomicBoolean(value));
      return false;
    }

    throw new ClassCastException(res + " is not a boolean value or atomic boolean");
  }

  /**获取boolean变量值
   *
   * @see ExtraVariableComp#getVar(String, Object)
   * @throws ClassCastException 如果变量已存在且不是boolean的封装类型或原子化引用*/
  default boolean getVar(String field, boolean def){
    Object res = getVar(field);
    if (res == null) return def;

    if (res instanceof AtomicBoolean i) return i.get();
    else if (res instanceof Boolean n) return n;

    throw new ClassCastException(res + " is not a boolean value or atomic boolean");
  }

  /**获取boolean变量值，并在变量不存在时初始化变量值
   *
   * @see ExtraVariableComp#getVar(String, Prov)
   * @throws ClassCastException 如果变量已存在且不是boolean的封装类型或原子化引用*/
  default boolean getVar(String field, Boolp initial){
    Object res = getVar(field);
    if (res == null){
      boolean b = initial.get();
      extra().put(field, new AtomicBoolean(b));
      return b;
    }

    if (res instanceof AtomicBoolean b) return b.get();
    else if (res instanceof Boolean n) return n;

    throw new ClassCastException(res + " is not a boolean value or atomic boolean");
  }

  /**使用处理函数处理boolean变量值，并使用返回值更新变量值
   *
   * @see ExtraVariableComp#handleVar(String, Function, Object)
   * @throws ClassCastException 如果变量已存在且不是boolean的封装类型或原子化引用*/
  default boolean handleVar(String field, BoolTrans handle, boolean def){
    boolean b;
    setVar(field, b = handle.get(getVar(field, def)));

    return b;
  }

  /**设置int类型变量值
   *
   * @see ExtraVariableComp#setVar(String, Object)
   * @throws ClassCastException 如果变量已存在且不是int的封装类型或原子化引用*/
  default int setVar(String field, int value){
    Object res = getVar(field);

    if (res instanceof AtomicInteger i){
      int r = i.get();
      i.set(value);
      return r;
    }
    else if (res instanceof Number n){
      extra().put(field, new AtomicInteger(value));
      return n.intValue();
    }
    else if (res == null){
      extra().put(field, new AtomicInteger(value));
      return 0;
    }

    throw new ClassCastException(res + " is not a number or atomic integer");
  }

  /**获取int变量值
   *
   * @see ExtraVariableComp#getVar(String, Object)
   * @throws ClassCastException 如果变量已存在且不是int的封装类型或原子化引用*/
  default int getVar(String field, int def){
    Object res = getVar(field);
    if (res == null) return def;

    if (res instanceof AtomicInteger i) return i.get();
    else if (res instanceof Number n) return n.intValue();

    throw new ClassCastException(res + " is not a number or atomic integer");
  }

  /**获取int变量值，并在变量不存在时初始化变量值
   *
   * @see ExtraVariableComp#getVar(String, Prov)
   * @throws ClassCastException 如果变量已存在且不是int的封装类型或原子化引用*/
  default int getVar(String field, Intp initial){
    Object res = getVar(field);
    if (res == null){
      int b = initial.get();
      extra().put(field, new AtomicInteger(b));
      return b;
    }

    if (res instanceof AtomicInteger i) return i.get();
    else if (res instanceof Number n) return n.intValue();

    throw new ClassCastException(res + " is not a number or atomic integer");
  }

  /**使用处理函数处理int变量值，并使用返回值更新变量值
   *
   * @see ExtraVariableComp#handleVar(String, Function, Object)
   * @throws ClassCastException 如果变量已存在且不是int的封装类型或原子化引用*/
  default int handleVar(String field, IntTrans handle, int def){
    int i;
    setVar(field, i = handle.get(getVar(field, def)));

    return i;
  }

  /**设置long类型变量值
   *
   * @see ExtraVariableComp#setVar(String, Object)
   * @throws ClassCastException 如果变量已存在且不是long的封装类型或原子化引用*/
  default long setVar(String field, long value){
    Object res = getVar(field);

    if (res instanceof AtomicLong l){
      long r = l.get();
      l.set(value);
      return r;
    }
    else if (res instanceof Number n){
      extra().put(field, new AtomicLong(value));
      return n.longValue();
    }
    else if (res == null){
      extra().put(field, new AtomicLong(value));
      return 0;
    }

    throw new ClassCastException(res + " is not a number or atomic long");
  }

  /**获取long变量值
   *
   * @see ExtraVariableComp#getVar(String, Object)
   * @throws ClassCastException 如果变量已存在且不是long的封装类型或原子化引用*/
  default long getVar(String field, long def){
    Object res = getVar(field);
    if (res == null) return def;

    if (res instanceof AtomicLong l) return l.get();
    else if (res instanceof Number n) return n.longValue();

    throw new ClassCastException(res + " is not a number or atomic long");
  }

  /**获取long变量值，并在变量不存在时初始化变量值
   *
   * @see ExtraVariableComp#getVar(String, Prov)
   * @throws ClassCastException 如果变量已存在且不是long的封装类型或原子化引用*/
  default long getVar(String field, Longp initial){
    Object res = getVar(field);
    if (res == null){
      long l = initial.get();
      extra().put(field, new AtomicLong(l));
      return l;
    }

    if (res instanceof AtomicLong l) return l.get();
    else if (res instanceof Number n) return n.longValue();

    throw new ClassCastException(res + " is not a number or atomic long");
  }

  /**使用处理函数处理long变量值，并使用返回值更新变量值
   *
   * @see ExtraVariableComp#handleVar(String, Function, Object)
   * @throws ClassCastException 如果变量已存在且不是long的封装类型或原子化引用*/
  default long handleVar(String field, LongTrans handle, long def){
    long l;
    setVar(field, l = handle.get(getVar(field, def)));

    return l;
  }

  /**设置float类型变量值
   *
   * @see ExtraVariableComp#setVar(String, Object)
   * @throws ClassCastException 如果变量已存在且不是float的封装类型或单元素float数组*/
  default float setVar(String field, float value){
    Object res = getVar(field);

    if (res instanceof float[] a && a.length == 1){
      float r = a[0];
      a[0] = value;
      return r;
    }
    else if (res instanceof Number n){
      extra().put(field, new float[]{value});
      return n.floatValue();
    }
    else if (res == null){
      extra().put(field, new float[]{value});
      return 0;
    }

    throw new ClassCastException(res + " is not a number or single float reference array");
  }

  /**获取float变量值
   *
   * @see ExtraVariableComp#getVar(String, Object)
   * @throws ClassCastException 如果变量已存在且不是float的封装类型或单元素float数组*/
  default float getVar(String field, float def){
    Object res = getVar(field);
    if (res == null) return def;

    if (res instanceof float[] f && f.length == 1) return f[0];
    else if (res instanceof Number n) return n.floatValue();

    throw new ClassCastException(res + " is not a number or single float reference array");
  }

  /**获取float变量值，并在变量不存在时初始化变量值
   *
   * @see ExtraVariableComp#getVar(String, Prov)
   * @throws ClassCastException 如果变量已存在且不是float的封装类型或单元素float数组*/
  default float getVar(String field, Floatp initial){
    Object res = getVar(field);
    if (res == null){
      float f = initial.get();
      extra().put(field, new float[]{f});
      return f;
    }

    if (res instanceof float[] l && l.length == 1) return l[0];
    else if (res instanceof Number n) return n.longValue();

    throw new ClassCastException(res + " is not a number or single float reference array");
  }

  /**使用处理函数处理float变量值，并使用返回值更新变量值
   *
   * @see ExtraVariableComp#handleVar(String, Function, Object)
   * @throws ClassCastException 如果变量已存在且不是float的封装类型或单元素float数组*/
  default float handleVar(String field, FloatTrans handle, float def){
    float trans;
    setVar(field, trans = handle.get(getVar(field, def)));

    return trans;
  }

  /**设置double类型变量值
   *
   * @see ExtraVariableComp#setVar(String, Object)
   * @throws ClassCastException 如果变量已存在且不是float的封装类型或单元素double数组*/
  default double setVar(String field, double value){
    Object res = getVar(field);

    if (res instanceof double[] a && a.length == 1){
      double r = a[0];
      a[0] = value;
      return r;
    }
    else if (res instanceof Number n){
      extra().put(field, new double[]{value});
      return n.doubleValue();
    }
    else if (res == null){
      extra().put(field, new double[]{value});
      return 0;
    }

    throw new ClassCastException(res + " is not a number or single double reference array");
  }

  /**获取double变量值
   *
   * @see ExtraVariableComp#getVar(String, Object)
   * @throws ClassCastException 如果变量已存在且不是float的封装类型或单元素double数组*/
  default double getVar(String field, double def){
    Object res = getVar(field);
    if (res == null) return def;

    if (res instanceof double[] f && f.length == 1) return f[0];
    else if (res instanceof Number n) return n.doubleValue();

    throw new ClassCastException(res + " is not a number or single double reference array");
  }

  /**获取double变量值，并在变量不存在时初始化变量值
   *
   * @see ExtraVariableComp#getVar(String, Prov)
   * @throws ClassCastException 如果变量已存在且不是double的封装类型或单元素double数组*/
  default double getVar(String field, Doublep initial){
    Object res = getVar(field);
    if (res == null){
      double d = initial.get();
      extra().put(field, new double[]{d});
      return d;
    }

    if (res instanceof double[] d && d.length == 1) return d[0];
    else if (res instanceof Number n) return n.doubleValue();

    throw new ClassCastException(res + " is not a number or single double reference array");
  }

  /**使用处理函数处理double变量值，并使用返回值更新变量值
   *
   * @see ExtraVariableComp#handleVar(String, Function, Object)
   * @throws ClassCastException 如果变量已存在且不是double的封装类型或单元素double数组*/
  default double handleVar(String field, DoubleTrans handle, double def){
    double d;
    setVar(field, d = handle.get(getVar(field, def)));

    return d;
  }
}
