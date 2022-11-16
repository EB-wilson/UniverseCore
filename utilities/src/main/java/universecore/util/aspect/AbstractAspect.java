package universecore.util.aspect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

/**容器切面的基类，一个切面应当具有拦截来源目标容器的add与remove入口的能力，否则则不应对切面分配来源容器，这样做会失去切面的意义
 * <p>切面容器是一种基于入口代理的特殊的容器成员处理对象，通过代理来源的add入口与remove入口，通过过滤器来完成切面成员的增删，避免依赖遍历来过滤目标，对Hash索引的容器尤其有效，
 * 可以更好的针对来源容器的某一类（由过滤器决定）对象统一的进行处理。通常，这个处理过程由触发器入口进行操作，同时这个类实现了{@link Iterable}，您也可以直接对元素进行遍历
 * <p>实现切面应当于构造函数中完成容器的分配，完成对来源的入口代理。
 *
 * @author EBwilson
 * @since 1.2*/
@SuppressWarnings("rawtypes")
public abstract class AbstractAspect<Type, Source> implements Iterable<Type>{
  protected final ArrayList<BaseTriggerEntry<?>> triggers = new ArrayList<>();
  protected final ArrayList<Type> children = new ArrayList<>();
  
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
    children.clear();
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
    for(Type child : children){
      entry.handle(child);
    }
  }
  
  /**尝试向切面中添加一个元素，如果过滤器通过则元素进入切面
   * @param added 尝试添加的元素*/
  public void add(Type added){
    if(filter(added)){
      children.add(added);
      if(entry != null) entry.accept(added);
    }
  }
  
  /**从切面中移除一个元素
   * @param removed 从切面移除的元素*/
  public void remove(Type removed){
    if(children.remove(removed) && exit != null) exit.accept(removed);
  }
  
  @Override
  public Iterator<Type> iterator(){
    return children.iterator();
  }
  
  public void apply(BaseTriggerEntry entry){
    if(apply != null) apply.accept(entry);
  }
  
  public void remove(BaseTriggerEntry entry){
    if(apply != null) remove.accept(entry);
  }
}
