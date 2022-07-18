package universecore.util.aspect;

import arc.Events;
import arc.func.Boolf;
import arc.struct.Seq;
import dynamilize.ArgumentList;
import dynamilize.DynamicClass;
import dynamilize.DynamicObject;
import mindustry.entities.EntityGroup;
import mindustry.game.EventType;
import mindustry.gen.*;
import universecore.UncCore;
import universecore.util.handler.ObjectHandler;

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
    return group.aspectGroup;
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
    all(Entityc.class),
    player(Player.class),
    bullet(Bullet.class),
    unit(Unit.class),
    build(Building.class),
    sync(Syncc.class),
    draw(Drawc.class),
    fire(Fire.class),
    puddle(Puddle.class),
    weather(WeatherState.class),
    label(WorldLabel.class);

    static {
      Events.on(EventType.ResetEvent.class, e -> Group.reset());
    }

    private final Class<?> type;
    
    private final Field field;
  
    Group(Class<?> type){
      this.type = type;
      try{
        field = Groups.class.getField(name());
        setAspect();
      }catch(NoSuchFieldException e){
        throw new RuntimeException(e);
      }
    }
  
    private final Seq<EntityAspect<Entityc>> aspects = new Seq<>();

    private DynamicClass GroupAspectType;
    private EntityGroup aspectGroup;
    private EntityGroup group;

    public static void reset(){
      for(Group group: values()){
        for(EntityAspect<Entityc> aspect: group.aspects){
          aspect.reset();
        }
      }
    }
  
    public void makeAspectType(EntityGroup<? extends Entityc> source){
      group = source;
      GroupAspectType = DynamicClass.get("GroupAspectType");

      GroupAspectType.setFinalFunc(
          "add",
          (DynamicObject<? extends EntityGroup<?>> self, ArgumentList args) -> {
        self.superPoint().invokeFunc("add", args);
        for(EntityAspect<Entityc> aspect : aspects){
          aspect.add(args.get(0));
        }
        return null;
      });

      GroupAspectType.setFinalFunc(
          "remove",
          (DynamicObject<? extends EntityGroup<?>> self, ArgumentList args) -> {
        self.superPoint().invokeFunc("remove", args);
        for(EntityAspect<Entityc> aspect : aspects){
          aspect.remove(args.<Entityc>get(0));
        }
        return null;
      });
    }
    
    private void setAspect(){
      try{
        EntityGroup<? extends Entityc> group = (EntityGroup<?>)field.get(null);
        if(GroupAspectType == null) makeAspectType(group);
        aspectGroup = UncCore.classes.getDynamicMaker().newInstance(
            group.getClass(),
            GroupAspectType,
            type,
            false,
            false
        );

        ObjectHandler.copyField(group, aspectGroup);

        field.set(null, aspectGroup);
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
