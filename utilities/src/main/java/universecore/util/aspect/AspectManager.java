package universecore.util.aspect;

import java.util.HashMap;
import java.util.HashSet;

/**切面管理器，保存了所有的切面和切面的触发控制器，是添加/移除切面的入口，通常创建一个公用单例使用，
 * 或者静态工厂<strong>{@code AspectManager getDefault()}</strong>获得的默认单例
 * </p>切面管理器之间，各自保存的切面和触发控制器都是相对独立的，如果你不清楚何时应该创建新的管理器，那么你应当使用默认单例而非新建切面管理器
 * 可通过方法{@code void addTriggerControl(BaseTriggerControl<?> control)}添加自定义的触发控制器
 * </p>关于触发控制器的声明，请参见：{@link BaseTriggerControl}
 * @author EBwilson
 * @since 1.2*/
@SuppressWarnings({"unchecked", "rawtypes"})
public class AspectManager{
  private static final AspectManager DEFAULT_INSTANCE = new AspectManager();

  private final HashMap<Class<?>, BaseTriggerControl> controls = new HashMap<>();
  private final HashSet<AbstractAspect<?, ?>> aspects = new HashSet<>();
  
  /**构造一个切面管理器，不携带任何触发控制器*/
  public AspectManager(){}

  /**获取切面管理器的默认单例*/
  public static AspectManager getDefault(){
    return DEFAULT_INSTANCE;
  }
  
  /**添加一个触发控制器，如果触发控制器的类型已经存在，那么将会覆盖原有的控制器
   * </p>关于触发控制器，请参见{@link BaseTriggerControl}
   * @param control 将被添加的触发控制器*/
  public void addTriggerControl(BaseTriggerControl<?> control){
    controls.put(control.getClass(), control);
  }
  
  /**向切面管理器中添加一个切面，并分配其触发器入口，如果切面已经存在，则不进行任何操作*/
  public <T extends AbstractAspect<?, ?>> T addAspect(T aspect){
    if(aspects.add(aspect)){
      aspect.apply = entry -> controls.get(entry.controlType).apply(entry);
      aspect.remove = entry -> controls.get(entry.controlType).remove(entry);
      for(BaseTriggerEntry entry : aspect.triggers){
        aspect.apply(entry);
      }
    }
    return aspect;
  }
  
  /**移除一个切面，并注销其创建的触发器入口，如果切面并不在切面中，则不进行任何操作*/
  public void removeAspect(AbstractAspect<?, ?> aspect){
    if(aspects.remove(aspect)){
      for(BaseTriggerEntry entry : aspect.triggers){
        aspect.remove(entry);
      }
    }
  }
}
