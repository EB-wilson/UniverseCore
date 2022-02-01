package universeCore.util.aspect;

import arc.func.Boolf;
import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;

import java.lang.reflect.Method;

/**arc库的容器的对应切面处理器集合，提供对常用容器的切面类型和获得这些类型实例的工厂方法*/
@SuppressWarnings({"unchecked", "rawtypes"})
public class ContainerAspects{
  public static class SeqAspect<Element> extends BaseContainerAspect<Element, Seq<Element>>{
    protected SeqContainerType contType = new SeqContainerType();
    
    protected SeqAspect(Seq<Element> source, Boolf<Element> filter, Cons<Seq<Element>> fieldSetter){
      super(source, filter, fieldSetter);
    }
  
    @Override
    public boolean filter(Element target){
      return false;
    }
  
    @Override
    public SeqContainerType contType(){
      return contType;
    }
  }
  
  public static class SeqContainerType extends BaseContainerAspect.BaseContainerType<Seq>{
    public SeqContainerType(){
      super(Seq.class);
    }
  
    @Override
    public void onAdd(BaseContainerAspect<Object, Seq> aspect, Seq seq, Object[] args){
      aspect.add(args[0]);
    }
  
    @Override
    public void onRemove(BaseContainerAspect<Object, Seq> aspect, Seq seq, Object[] args){
      aspect.remove(seq.get((int) args[0]));
    }
  
    @Override
    public Method getAddEntry(){
      try{
        return Seq.class.getMethod("add", Object.class);
      }catch(NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
  
    @Override
    public Method getRemoveEntry(){
      try{
        return Seq.class.getMethod("remove", int.class);
      }catch(NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
  }
  
  public static class ObjectSetAspect<Element> extends BaseContainerAspect<Element, ObjectSet<Element>>{
    protected ObjectSetContainerType contType = new ObjectSetContainerType();
    
    protected ObjectSetAspect(ObjectSet<Element> source, Boolf<Element> filter, Cons<ObjectSet<Element>> fieldSetter){
      super(source, filter, fieldSetter);
    }
  
    @Override
    public boolean filter(Element target){
      return false;
    }
    
    @Override
    public ObjectSetContainerType contType(){
      return contType;
    }
  }
  
  public static class ObjectSetContainerType extends BaseContainerAspect.BaseContainerType<ObjectSet>{
    public ObjectSetContainerType(){
      super(ObjectSet.class);
    }
  
    @Override
    public void onAdd(BaseContainerAspect<Object, ObjectSet> aspect, ObjectSet set, Object[] args){
      aspect.add(args[0]);
    }
  
    @Override
    public void onRemove(BaseContainerAspect<Object, ObjectSet> aspect, ObjectSet set, Object[] args){
      aspect.remove(args[0]);
    }
  
    @Override
    public Method getAddEntry(){
      try{
        return ObjectSet.class.getMethod("add", Object.class);
      }catch(NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
  
    @Override
    public Method getRemoveEntry(){
      try{
        return ObjectMap.class.getMethod("remove", Object.class);
      }catch(NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
  }
  
  public static class ObjectMapAspect<Key, Element> extends BaseContainerAspect<Element, ObjectMap<Key, Element>>{
    protected ObjectMapContainerType contType = new ObjectMapContainerType();
    
    protected ObjectMapAspect(ObjectMap<Key, Element> source, Boolf<Element> filter, Cons<ObjectMap<Key, Element>> fieldSetter){
      super(source, filter, fieldSetter);
    }
  
    @Override
    public boolean filter(Element target){
      return false;
    }
    
    @Override
    public ObjectMapContainerType contType(){
      return contType;
    }
  }
  
  public static class ObjectMapContainerType extends BaseContainerAspect.BaseContainerType<ObjectMap>{
    public ObjectMapContainerType(){
      super(ObjectMap.class);
    }
  
    @Override
    public void onAdd(BaseContainerAspect<Object, ObjectMap> aspect, ObjectMap objectMap, Object[] args){
      aspect.add(args[1]);
    }
  
    @Override
    public void onRemove(BaseContainerAspect<Object, ObjectMap> aspect, ObjectMap objectMap, Object[] args){
      aspect.remove(objectMap.get(args[0]));
    }
  
    @Override
    public Method getAddEntry(){
      try{
        return ObjectMap.class.getMethod("put", Object.class, Object.class);
      }catch(NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
  
    @Override
    public Method getRemoveEntry(){
      try{
        return ObjectMap.class.getMethod("remove", Object.class);
      }catch(NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
  }
}
