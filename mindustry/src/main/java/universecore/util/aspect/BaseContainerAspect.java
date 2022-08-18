package universecore.util.aspect;

import arc.func.Boolf;
import arc.func.Cons;
import arc.struct.ObjectMap;
import dynamilize.DynamicClass;
import universecore.UncCore;

import java.lang.reflect.Method;

/**arc库容器的切面基类，可对arc容器及容器的子类进行切面化管理，有一个内部抽象类{@link BaseContainerType}控制对来源容器的代理入口策略。
 * <p>针对容器实现的入口实例应当在方法{@link BaseContainerAspect#contType()}被返回，其将被用于构造容器的入口代理实例
 * </p><strong>注意，此切面并未覆盖来源容器的字段，你应当在构造容器切面时，在传入lambda中将来源字段设置为此切面的入口代理实例</strong>
 * @author EBwilson
 * @since 1.2*/
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BaseContainerAspect<Type, Cont> extends AbstractAspect<Type, Cont>{
  protected final Cont proxiedCont;
  protected final Boolf<Type> filter;
  
  protected BaseContainerAspect(Cont source, Boolf<Type> filter, Cons<Cont> fieldSetter){
    super(source);
    this.filter = filter;
    proxiedCont = (Cont)contType().instance(source);
    contType().addAspect(proxiedCont, this);
    fieldSetter.get(proxiedCont);
  }
  
  public abstract BaseContainerType contType();
  
  @Override
  public Cont instance(){
    return proxiedCont;
  }
  
  @Override
  public boolean filter(Type target){
    return filter.get(target);
  }
  
  @Override
  public void releaseAspect(){
    super.releaseAspect();
    contType().removeAspect(proxiedCont);
  }
  
  /**容器类型的模板，用于创建对目标容器的入口建立代理，不同的容器入口可能有各自的名称及实现，而此类的具体实现就是简单的实现对add入口，remove入口的代理*/
  public abstract static class BaseContainerType<Cont>{
    protected final ObjectMap<Object, BaseContainerAspect<?, Cont>> aspectMap = new ObjectMap<>();
    protected final Class<Cont> type;

    protected DynamicClass AspectType;

    protected BaseContainerType(Class<Cont> type){
      this.type = type;
    }
    
    public DynamicClass getEntryProxy(Class<? extends Cont> type){
      if(AspectType != null) return AspectType;

      AspectType = DynamicClass.get(type.getSimpleName() + "Aspect");
      for(Method method: getAddEntry()){
        AspectType.setFunction(method.getName(), (self, supe, args) -> {
          BaseContainerAspect<Object, Cont> aspect = (BaseContainerAspect<Object, Cont>) aspectMap.get(self);
          if(aspect != null) onAdd(aspect, self.self(), args.args());
          return supe.invokeFunc(method.getName(), args);
        }, method.getParameterTypes());
      }
      for(Method method: getRemoveEntry()){
        AspectType.setFunction(method.getName(), (self, supe, args) -> {
          BaseContainerAspect<Object, Cont> aspect = (BaseContainerAspect<Object, Cont>) aspectMap.get(self);
          if(aspect != null) onRemove(aspect, self.self(), args.args());
          return supe.invokeFunc(method.getName(), args);
        }, method.getParameterTypes());
      }

      return AspectType;
    }
    
    public Cont instance(Cont source){
      return instance((Class<Cont>) source.getClass());
    }
    
    public Cont instance(Class<? extends Cont> type){
      if(!this.type.isAssignableFrom(type)) throw new IllegalArgumentException("can not create a disassignable class: " + type + " instance");
      return UncCore.classes.getDynamicMaker().newInstance(type, getEntryProxy(type)).self();
    }
    
    public void addAspect(Cont cont, BaseContainerAspect<?, Cont> aspect){
      aspectMap.put(cont, aspect);
    }
    
    public void removeAspect(Cont cont){
      aspectMap.remove(cont);
    }
  
    /**add入口的代理调用实现方法，在目标来源的add入口被调用时执行，子类实现此方法应当确保能够将被添加的目标正确经过过滤器，并正确的抉择是否进入切面*/
    public abstract void onAdd(BaseContainerAspect<Object, Cont> aspect, Cont cont, Object[] args);
  
    /**remove入口的代理调用实现方法，在目标来源的remove入口被调用时执行，子类实现此方法应当确保能够将被移除的目标正确从切面中删除*/
    public abstract void onRemove(BaseContainerAspect<Object, Cont> aspect, Cont cont, Object[] args);
    
    /**获得来源容器的add入口的方法对象，子类实现应当返回容器仅在添加对象时会调用的<strong>public或者protected</strong>方法，这些方法应在添加对象时必定会调用并正确的传入被添加对象
     * <p>注意，入口方法可能存在方法迭代的结构，存在迭代调用的方法入口应当调用最后一级的方法，避免将迭代中的两个或更多环节标记为入口*/
    public abstract Method[] getAddEntry();
  
    /**获得来源容器的remove入口的方法对象，子类实现应当返回容器仅在删除对象时会调用的<strong>public或者protected</strong>方法，这些方法应在添加对象时必定会调用并正确的传入被移除对象
     * <p>注意，入口方法可能存在方法迭代的结构，存在迭代调用的方法入口应当调用最后一级的方法，避免将迭代中的两个或更多环节标记为出口*/
    public abstract Method[] getRemoveEntry();
  }
}
