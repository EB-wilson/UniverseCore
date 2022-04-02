package universecore.util.aspect;

import arc.Events;
import arc.func.Boolf;
import arc.struct.Seq;
import mindustry.entities.EntityGroup;
import mindustry.game.EventType;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import universecore.UncCore;
import universecore.util.proxy.BaseProxy;

import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
public class EntityAspect<EntityType extends Entityc> extends AbstractAspect<EntityType, EntityGroup<EntityType>>{
  private final Group group;
  private final Boolf<EntityType> filter;
  
  public EntityAspect(Group group, Boolf<EntityType> filter){
    super((EntityGroup<EntityType>)group.group);
    this.group = group;
    this.filter = filter;
    group.addAspect((EntityAspect<Entityc>) this);
  }
  
  @Override
  public EntityGroup<EntityType> instance(){
    return group.proxiedGroup;
  }
  
  @Override
  public boolean filter(EntityType target){
    return filter.get(target);
  }
  
  @Override
  public void releaseAspect(){
    super.releaseAspect();
    group.removeAspect((EntityAspect<Entityc>) this);
  }
  
  @SuppressWarnings("rawtypes")
  public enum Group{
    all,
    player,
    bullet,
    unit,
    build,
    sync,
    draw,
    fire,
    puddle,
    weather;

    static {
      Events.on(EventType.ResetEvent.class, e -> Group.reset());
    }
    
    private final Field field;
  
    Group(){
      try{
        field = Groups.class.getField(name());
        setProxy();
      }catch(NoSuchFieldException e){
        throw new RuntimeException(e);
      }
    }
  
    private final Seq<EntityAspect<Entityc>> aspects = new Seq<>();
    
    private BaseProxy proxy;
    private EntityGroup proxiedGroup;
    private EntityGroup group;

    public static void reset(){
      for(Group group: values()){
        for(EntityAspect<Entityc> aspect: group.aspects){
          aspect.reset();
        }
      }
    }
  
    public void setSource(EntityGroup<? extends Entityc> source){
      group = source;
      proxy = UncCore.classes.getProxy(source.getClass(), "aspect_" + name());

      if(proxy.isInitialized()) return;
  
      try{
        proxy.addMethodProxy(EntityGroup.class.getMethod("add", Entityc.class), (self, superHandler, args) -> {
          superHandler.callSuper(self, args);
          for(EntityAspect<Entityc> aspect : aspects){
            aspect.add((Entityc) args[0]);
          }
          return null;
        });
        proxy.addMethodProxy(EntityGroup.class.getMethod("remove", Entityc.class), (self, superHandler, args) -> {
          superHandler.callSuper(self, args);
          for(EntityAspect<Entityc> aspect : aspects){
            aspect.remove((Entityc) args[0]);
          }
          return null;
        });
      }catch(NoSuchMethodException e){
        throw new RuntimeException(e);
      }
    }
    
    private void setProxy(){
      try{
        EntityGroup<? extends Entityc> group = (EntityGroup<?>)field.get(null);
        setSource(group);
        proxiedGroup = (EntityGroup) proxy.create(group);
        field.set(null, proxiedGroup);
        for(Entityc e : group){
          for(EntityAspect<Entityc> aspect : aspects){
            aspect.add(e);
          }
        }
      }catch(IllegalAccessException e){
        throw new RuntimeException(e);
      }
    }
    
    public void addAspect(EntityAspect<Entityc> aspect){
      aspects.add(aspect);
    }
    
    public void removeAspect(EntityAspect<Entityc> aspect){
      aspects.remove(aspect);
    }
  }
}
