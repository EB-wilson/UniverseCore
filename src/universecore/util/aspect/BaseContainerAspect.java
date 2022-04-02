package universecore.util.aspect;

import arc.func.Boolf;
import arc.func.Cons;
import arc.struct.ObjectMap;
import universecore.UncCore;
import universecore.util.proxy.BaseProxy;

import java.lang.reflect.Method;

/**arc库容器的切面基类，有一个内部抽象类保存对来源容器的代理入口策略，针对容器实现内部类应当在抽象方法contType被返回，其将被用于构造入口代理实例
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
    
    protected BaseContainerType(Class<Cont> type){
      this.type = type;
    }
    
    public BaseProxy<Cont> getProxy(Class<? extends Cont> type){
      if(!type.isAssignableFrom(type)) throw new IllegalArgumentException("try create aspect type use a disassignable type: " + type);
      BaseProxy<? extends Cont> result = UncCore.classes.getProxy(type, "aspect");
      result.addMethodProxy(getAddEntry(), (self, superHandle, args) -> {
        BaseContainerAspect<Object, Cont> aspect = (BaseContainerAspect<Object, Cont>) aspectMap.get(self);
        if(aspect != null) onAdd(aspect, self, args);
        return superHandle.callSuper(self, args);
      });
      result.addMethodProxy(getRemoveEntry(), (self, superHandle, args) -> {
        BaseContainerAspect<Object, Cont> aspect = (BaseContainerAspect<Object, Cont>) aspectMap.get(self);
        if(aspect != null) onRemove(aspect, self, args);
        return superHandle.callSuper(self, args);
      });
      return (BaseProxy<Cont>) result;
    }
    
    public Cont instance(Cont source){
      return getProxy((Class<Cont>) source.getClass()).create(source);
    }
    
    public Cont instance(Class<? extends Cont> type){
      if(!this.type.isAssignableFrom(type)) throw new IllegalArgumentException("can not create a disassignable class: " + type + " instance");
      return getProxy(type).create(null);
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
    
    /**获得来源容器的add入口的方法对象，子类实现应当返回容器仅在添加对象时会调用的<strong>public或者protected</strong>方法，且添加对象时必定会访问并正确的传入被添加的对象*/
    public abstract Method getAddEntry();
  
    /**获得来源容器的remove入口的方法对象，子类实现应当返回容器仅在删除对象时会调用的<strong>public或者protected</strong>方法，且添加对象时必定会访问并正确的传入被移除的对象*/
    public abstract Method getRemoveEntry();
  }
}
