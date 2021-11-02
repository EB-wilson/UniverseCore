package universeCore.world.blockModule;

import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.meta.BlockStatus;
import mindustry.world.modules.ConsumeModule;
import universeCore.entityComps.blockComps.*;
import universeCore.world.consumers.*;

import java.util.ArrayList;

/**生产者的消耗器模块，用于集中处理方块的材料需求等，提供了可选需求以及其特殊的触发器
 * @author EBwilson 😀*/
@SuppressWarnings("all")
public class BaseConsumeModule extends ConsumeModule{
  protected final ConsumerBuildComp entity;
  protected final BaseConsumers[] consumes;
  protected final BaseConsumers[] optionalCons;
  protected final float[] optProgress;
  
  public boolean acceptAll;
  
  public BaseConsumers current;
  public BaseConsumers optionalCurr;
  public boolean valid;
  public Seq<ObjectMap<UncConsumeType<?>, ObjectSet<Object>>> filter = new Seq<>();
  public ObjectMap<UncConsumeType<?>, ObjectSet<Object>> optionalFilter = new ObjectMap<>();
  public ObjectMap<UncConsumeType<?>, ObjectSet<Object>> allFilter = new ObjectMap<>();
  
  private float powerCons;

  public BaseConsumeModule(ConsumerBuildComp entity, ArrayList<BaseConsumers> cons, ArrayList<BaseConsumers> optional){
    super(entity.getBuilding());
    this.entity = entity;
    consumes = cons.size() > 0? cons.toArray(new BaseConsumers[0]): null;
    optionalCons = optional.size() > 0? optional.toArray(new BaseConsumers[0]): null;
    optProgress = new float[optionalCons == null? 0: optionalCons.length];
    current = consumes != null && entity.consumeCurrent() != -1 ? consumes[entity.consumeCurrent()] : null;
    applyFilter();
  }

  public void build(Table table){
    if(current != null) for(BaseConsume cons: current.all()){
      cons.build(entity.getBuilding(), table);
    }
  }
  
  public BaseConsumers[] get(){
    return consumes;
  }
  
  public BaseConsumers[] getOptional(){
    return optionalCons;
  }
  
  public BlockStatus status(){
    if(!entity.getBuilding().shouldConsume()){
      return BlockStatus.noOutput;
    }

    if(!valid || !entity.productionValid()){
      return BlockStatus.noInput;
    }

    return BlockStatus.active;
  }
  
  public boolean hasConsume(){
    return consumes != null;
  }
  
  public boolean hasOptional(){
    return optionalCons != null;
  }
  
  public void applyFilter(){
    if(optionalCons != null){
      for(BaseConsumers cons: optionalCons){
        for(BaseConsume c: cons.all()){
          if(c.filter(entity.getBuilding()) != null){
            if(optionalFilter.get(c.type()) == null){
              optionalFilter.put(c.type(), new ObjectSet<>());
            }
            optionalFilter.get(c.type()).addAll(c.filter(entity.getBuilding()));
          }
        }
      }
    }
    
    if(consumes != null){
      for(int l=0; l<consumes.length; l++){
        BaseConsumers cons = consumes[l];
        ObjectMap<UncConsumeType<?>, ObjectSet<Object>> map = new ObjectMap<>();
        filter.add(map);
        for(BaseConsume c: cons.all()){
          if(c.filter(entity.getBuilding()) != null){
            map.put(c.type(), ObjectSet.with(c.filter(entity.getBuilding())));
            if(allFilter.get(c.type()) == null){
              allFilter.put(c.type(), new ObjectSet<>());
            }
            allFilter.get(c.type()).addAll(c.filter(entity.getBuilding()));
          }
        }
      }
    }
  }
  
  public float getPowerUsage(){
    return powerCons*entity.consumeMultiplier(current.get(UncConsumeType.power));
  }
  
  public void setCurrent(){
    current = consumes[entity.consumeCurrent()];
  }
  
  @Override
  public void update(){
    current = null;
    powerCons = 0;
    if((!hasOptional() && !hasConsume())) return;
    boolean docons = entity.shouldConsume() && entity.productionValid();
    
    //只在选中消耗列表时才进行消耗更新
    if(entity.consumeCurrent() >= 0 && consumes != null){
      boolean preValid = valid();
      valid = true;
      
      setCurrent();
      if(current != null){
        valid &= current.valid.get(entity);
        for(BaseConsume cons: current.all()){
          if(cons instanceof UncConsumePower) powerCons += ((UncConsumePower) cons).usage;
          valid &= cons.valid(entity.getBuilding());
          if(docons && preValid && cons.valid(entity.getBuilding())){
            cons.update(entity.getBuilding());
          }
        }
      }
    }
    
    //更新可选消耗列表
    if(optionalCons != null){
      for(int id=0; id<optionalCons.length; id++){
        BaseConsumers cons = optionalCons[id];
        
        boolean optionalValid = cons.valid.get(entity);
        for(BaseConsume c: cons.all()){
          optionalValid &= c.valid(entity.getBuilding());
        }
        if(optionalValid){
          optionalCurr = cons;
          
          for(BaseConsume c: cons.all()){
            if(docons){
              c.update(entity.getBuilding());
              if(c instanceof UncConsumePower) powerCons += ((UncConsumePower) c).usage;
            }
          }
          optProgress[id] += 1/cons.craftTime*entity.consDelta(optionalCurr);
          if(optProgress[id] >= 1){
            optProgress[id] %= 1;
            triggerOpt(id);
          }
          cons.optionalDef.get(entity, cons);
          if(entity.getConsumerBlock().oneOfOptionCons()) break;
        }
      }
    }
  }
  
  /**获取指定索引的消耗列表*/
  public BaseConsumers get(int index){
    return consumes[index];
  }
  
  /**获取指定索引处的可选消耗列表*/
  public BaseConsumers getOptional(int index){
    return index < optionalCons.length? optionalCons[index]: null;
  }
  
  /**触发一次所有主要消耗项的trigger方法*/
  public void trigger(){
    if(current != null){
      for(BaseConsume cons: current.all()){
        cons.consume(entity.getBuilding());
      }
      current.trigger.get(entity);
    }
  }
  
  /**触发一次所有可选消耗项的trigger方法*/
  public void triggerOpt(int id){
    if(optionalCons != null && optionalCons.length > id){
      BaseConsumers cons = optionalCons[id];
      for(BaseConsume c: cons.all()){
        c.consume(entity.getBuilding());
      }
      cons.trigger.get(entity);
    };
  }
  
  /**当前消耗列表除指定消耗项以外是否其他全部可用*/
  public boolean excludeValid(UncConsumeType type){
    boolean temp = true;
    for(BaseConsume cons: current.all()){
      if(cons.type() == type) continue;
      temp &= cons.valid(entity.getBuilding());
    }
    return temp;
  }
  
  /**当前消耗列表是否可用*/
  public boolean valid(){
    return valid && entity.shouldConsume() && entity.getBuilding().enabled;
  }
  
  /**当前消耗列表指定消耗项是否可用*/
  public boolean valid(UncConsumeType type){
    return current.get(type) != null && current.get(type).valid(entity.getBuilding());
  }
  
  /**制定的消耗列表是否可用*/
  public boolean valid(int index){
    if(index >= consumes.length) return false;
    
    BaseConsumers cons = consumes[index];
    for(BaseConsume c: cons.all()){
      if(!c.valid(entity.getBuilding())) return false;
    }
    
    return true;
  }

  /**过滤器，将判断对当前选中的区域指定type下对输入的对象是否接受
  * 若可选过滤器已添加目标对象同样返回true
  * @param type 过滤器种类
  * @param target 通过过滤器的目标对象
  * @return 布尔值，是否接受此对象
  * */
  public boolean filter(UncConsumeType<?> type, Object target){
    return optionalFilter.containsKey(type) && optionalFilter.get(type).contains(target) || //可选的
        (acceptAll && allFilter.containsKey(type) && allFilter.get(type).contains(target)) ||
        (entity.consumeCurrent() >= 0 && filter.get(entity.consumeCurrent()).containsKey(type)
            && filter.get(entity.consumeCurrent()).get(type).contains(target));
  }

  @Override
  public void write(Writes write) {
    write.bool(valid);
  }

  @Override
  public void read(Reads read){
    valid = read.bool();
  }
}
