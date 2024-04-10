package universecore.util.aspect;

import java.util.*;
import java.util.function.Consumer;

/**容器切面的基类，一个切面应当具有拦截来源目标容器的add与remove入口的能力，否则则不应对切面分配来源容器，这样做会失去切面的意义
 * <p>切面容器是一种基于入口代理的特殊的容器成员处理对象，通过代理来源的add入口与remove入口，通过过滤器来完成切面成员的增删，避免依赖遍历来过滤目标，对Hash索引的容器尤其有效，
 * 可以更好的针对来源容器的某一类（由过滤器决定）对象统一的进行处理。通常，这个处理过程由触发器入口进行操作，同时这个类实现了{@link Iterable}，您也可以直接对元素进行遍历
 * <p>实现切面应当于构造函数中完成容器的分配，完成对来源的入口代理。
 *
 * @author EBwilson
 * @since 1.2
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractAspect<Type, Source> implements Iterable<Type>{
  protected final List<BaseTriggerEntry<?>> triggers = new ArrayList<>();
  protected final Set<Type> children = new LinkedHashSet<>();

  private final Cache iterateCache = new Cache();
  private boolean modified = true;//初始化
  
  protected Consumer<BaseTriggerEntry<?>> apply, remove;
  protected Source source;

  protected Consumer<Type> entry;
  protected Consumer<Type> exit;
  
  protected AbstractAspect(Source source){
    this.source = source;
  }

  /**获得该切面应用的有效容器实例*/
  public abstract Source instance();

  /**切面处理的元素过滤器*/
  public abstract boolean filter(Type target);
  
  /**当切面不再使用后，调用此方法来释放切面，具体的释放内容由子类进行实现，重写应当能够释放切面对成员的所有引用以及切面产生的各种副作用
   * <p>调用此方法前请确认已经从<strong>所有</strong>切面管理器卸载此切面，否则可能会造成不可预测的异常*/
  public void releaseAspect(){
    children.clear();
    triggers.clear();
    apply = null;
    remove = null;
    source = null;
  }

  /**设置切面的入口触发器，当元素被添加进切面时调用触发器
   * @param entry 入口触发器，传入进入切面的元素*/
  public AbstractAspect<Type, Source> setEntryTrigger(Consumer<Type> entry){
    this.entry = entry;
    return this;
  }

  /**设置切面的出口触发器，当元素从切面退出时调用触发器
   * @param exit 出口触发器，传入退出切面的元素*/
  public AbstractAspect<Type, Source> setExitTrigger(Consumer<Type> exit){
    this.exit = exit;
    return this;
  }

  /**将切面重置，这会清空切面保存的元素*/
  public void reset(){
    for(Type child: children){
      if(exit != null) exit.accept(child);
    }
    children.clear();
    modified = true;
  }
  
  /**设置一个触发器入口，关于触发器入口，请参见{@link BaseTriggerEntry}
   * @param trigger 触发器入口*/
  public AbstractAspect<Type, Source> setTrigger(BaseTriggerEntry<Type> trigger){
    triggers.add(trigger);
    trigger.aspect = this;
    apply(trigger);
    return this;
  }
  
  /**移除一个触发器入口，关于触发器入口，请参见{@link BaseTriggerEntry}
   * @param trigger 触发器入口*/
  public void removeTrigger(BaseTriggerEntry<Type> trigger){
    triggers.remove(trigger);
    remove(trigger);
  }
  
  /**对切面的所有子元素执行触发器处理
   * @param entry 执行的目标触发器*/
  public void run(BaseTriggerEntry<Type> entry){
    for(Type child: this){
      entry.handle(child);
    }
  }
  
  /**尝试向切面中添加一个元素，如果过滤器通过则元素进入切面
   * @param added 尝试添加的元素*/
  public void add(Type added){
    if(filter(added)){
      if(children.add(added) && entry != null){
        entry.accept(added);
        modified = true;
      }
    }
  }
  
  /**从切面中移除一个元素
   * @param removed 从切面移除的元素*/
  public void remove(Type removed){
    if(children.remove(removed) && exit != null){
      exit.accept(removed);
      modified = true;
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Type> iterator(){//规避并发修改，切面的行为有可能使元素从切面移除，需要一个缓冲区回避此问题
    if (modified){
      if (iterateCache.arr== null || iterateCache.arr.length < children.size()){
        iterateCache.arr = (Type[]) new Object[children.size()];
      }

      int i = 0;
      for (Type child : children) {
        iterateCache.arr[i++] = child;
      }

      modified = false;
    }

    return iterateCache.iterator();
  }
  
  public void apply(BaseTriggerEntry entry){
    if(apply != null) apply.accept(entry);
  }
  
  public void remove(BaseTriggerEntry entry){
    if(remove != null) remove.accept(entry);
  }

  private class Cache implements Iterable<Type>{
    Type[] arr;
    int index;

    Iterator<Type> itr = new Iterator<>() {
      @Override
      public boolean hasNext() {
        return index < arr.length;
      }

      @Override
      public Type next() {
        return arr[index++];
      }
    };

    @Override
    public Iterator<Type> iterator() {
      index = 0;
      return itr;
    }
  }
}
