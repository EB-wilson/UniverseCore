package universeCore.util.aspect;

import arc.func.Cons;
import arc.struct.Seq;
import universeCore.util.aspect.triggers.BaseTriggerEntry;

import java.util.Iterator;

/**切面容器的基类，一个切面应当具有拦截来源目标容器的add与remove入口的能力，否则则不应对切面分配来源容器，这样做会失去切面的意义
 * </p>切面容器是一种基于入口代理的特殊的容器成员处理对象，通过代理来源的add入口与remove入口，通过过滤器来完成切面成员的增删，避免依赖遍历来过滤目标，对Hash索引的容器尤其有效
 * 可以更好的针对来源容器的某一类（由过滤器决定）对象统一的进行处理，通常，这个处理方式由触发器入口进行处理，同时这个类实现了默认迭代器，你也可以直接对元素进行遍历
 * </p>默认提供了对{@link mindustry.gen.Groups}内所有{@link mindustry.entities.EntityGroup}，常用的arc库容器，以及java集合框架的入口代理
 * </p>实现切面应当于构造函数中完成容器的分配，完成对来源的入口代理，关于来源入口的代理实现，请参见此类的默认实现的样例
 * @author EBwilson
 * @since 1.2*/
@SuppressWarnings("rawtypes")
public abstract class AbstractAspect<Type, Source> implements Iterable<Type>{
  protected final Seq<BaseTriggerEntry<?>> triggers = new Seq<>();
  protected final Seq<Type> children = new Seq<>();
  
  protected Cons<BaseTriggerEntry<?>> apply, remove;
  protected Source source;
  
  protected AbstractAspect(Source source){
    this.source = source;
  }
  
  public abstract Source instance();
  
  public abstract boolean filter(Type target);
  
  /**当切面不再使用后，调用此方法来释放切面，具体的释放内容由子类进行实现，重写应当能够释放切面对成员的所有引用以及切面产生的各种副作用
   * </p>调用此方法前请确认已经从<strong>所有</strong>切面管理器卸载此切面，否则可能会造成不可预测的异常*/
  public void releaseAspect(){
    children.clear();
    triggers.clear();
    apply = null;
    remove = null;
    source = null;
  }
  
  /**设置一个触发器入口，关于触发器入口，请参见{@link BaseTriggerEntry}
   * @param trigger 触发器入口*/
  public void setTrigger(BaseTriggerEntry<Type> trigger){
    triggers.add(trigger);
    trigger.aspect = this;
    apply(trigger);
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
    if(filter(added)) children.add(added);
  }
  
  /**从切面中移除一个元素
   * @param removed 从切面移除的元素*/
  public void remove(Type removed){
    children.remove(removed);
  }
  
  @Override
  public Iterator<Type> iterator(){
    return children.iterator();
  }
  
  public void apply(BaseTriggerEntry entry){
    if(apply != null) apply.get(entry);
  }
  
  public void remove(BaseTriggerEntry entry){
    if(apply != null) remove.get(entry);
  }
}
