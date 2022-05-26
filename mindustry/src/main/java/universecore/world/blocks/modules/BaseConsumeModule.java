package universecore.world.blocks.modules;

import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ctype.Content;
import mindustry.world.meta.BlockStatus;
import mindustry.world.modules.ConsumeModule;
import universecore.components.blockcomp.ConsumerBuildComp;
import universecore.world.consumers.BaseConsume;
import universecore.world.consumers.BaseConsumers;
import universecore.world.consumers.UncConsumePower;
import universecore.world.consumers.UncConsumeType;

import java.util.ArrayList;

/**生产者的消耗器模块，用于集中处理方块的材料需求等，提供了可选需求以及其特殊的触发器
 * @author EBwilson 😀*/
@SuppressWarnings("all")
public class BaseConsumeModule extends ConsumeModule{
  protected final ConsumerBuildComp entity;
  protected final ObjectMap<BaseConsumers, float[]> optProgress = new ObjectMap<>();
  
  public BaseConsumers current;
  public BaseConsumers optionalCurr;
  public boolean valid;
  public Seq<ObjectMap<UncConsumeType<?>, Bits>> filter = new Seq<>();
  public ObjectMap<UncConsumeType<?>, Bits> optionalFilter = new ObjectMap<>();
  public ObjectMap<UncConsumeType<?>, Bits> allFilter = new ObjectMap<>();
  
  private float powerCons;

  public BaseConsumeModule(ConsumerBuildComp entity){
    super(entity.getBuilding());
    this.entity = entity;
    current = entity.getConsumerBlock().consumers() != null && entity.consumeCurrent() != -1?
        entity.getConsumerBlock().consumers().get(entity.consumeCurrent()): null;
    applyFilter();
  }

  public void build(Table table){
    if(current != null) for(BaseConsume cons: current.all()){
      cons.build(entity.getBuilding(ConsumerBuildComp.class), table);
    }
  }
  
  public ArrayList<BaseConsumers> get(){
    return entity.getConsumerBlock().consumers();
  }
  
  public ArrayList<BaseConsumers> getOptional(){
    return entity.getConsumerBlock().optionalCons();
  }
  
  public BlockStatus status(){
    if(!entity.getBuilding(ConsumerBuildComp.class).shouldConsume()){
      return BlockStatus.noOutput;
    }

    if(!valid || !entity.productionValid()){
      return BlockStatus.noInput;
    }

    return BlockStatus.active;
  }
  
  public boolean hasConsume(){
    return !get().isEmpty();
  }
  
  public boolean hasOptional(){
    return !getOptional().isEmpty();
  }
  
  public void applyFilter(){
    if(getOptional() != null){
      Bits f, t;
      
      for(BaseConsumers cons: getOptional()){
        for(BaseConsume c: cons.all()){
          if((f = c.filter(entity.getBuilding(ConsumerBuildComp.class))) != null){
            if((t = optionalFilter.get(c.type())) == null){
              optionalFilter.put(c.type(), f);
            }
            else t.or(f);
          }
        }
      }
    }
    
    if(get() != null){
      Bits f, t;
      for(BaseConsumers cons: get()){
        ObjectMap<UncConsumeType<?>, Bits> map = new ObjectMap<>();
        filter.add(map);
        for(BaseConsume c: cons.all()){
          if((f = c.filter(entity.getBuilding(ConsumerBuildComp.class))) != null){
            map.put(c.type(), f);
            if((t = allFilter.get(c.type())) == null){
              Bits n = new Bits(f.length());
              n.set(f);
              allFilter.put(c.type(), n);
            }
            else t.or(f);
          }
        }
      }
    }
  }

  public float getPowerUsage(){
    return powerCons*((BaseConsume<ConsumerBuildComp>)current.get(UncConsumeType.power)).multiple(entity);
  }
  
  public void setCurrent(){
    current = get().get(entity.consumeCurrent());
  }
  
  @Override
  public void update(){
    current = null;
    
    powerCons = 0;
    if((!hasOptional() && !hasConsume())) return;
    boolean docons = entity.shouldConsume() && entity.productionValid();
    
    //只在选中消耗列表时才进行消耗更新
    if(entity.consumeCurrent() >= 0 && get() != null){
      boolean preValid = valid();
      valid = true;
      
      setCurrent();
      if(current != null){
        valid &= current.valid.get(entity);
        for(BaseConsume cons: current.all()){
          if(cons instanceof UncConsumePower) powerCons += ((UncConsumePower) cons).usage;
          valid &= cons.valid(entity.getBuilding(ConsumerBuildComp.class));
          if(docons && preValid && cons.valid(entity.getBuilding(ConsumerBuildComp.class))){
            cons.update(entity.getBuilding(ConsumerBuildComp.class));
          }
        }
      }
    }
    
    //更新可选消耗列表
    if(getOptional() != null){
      BaseConsumers cons;
      boolean onlyOne = entity.getConsumerBlock().oneOfOptionCons();
      for(int id=0; id<getOptional().size(); id++){
        cons = getOptional().get(id);
        
        boolean optionalValid = cons.valid.get(entity);
        for(BaseConsume c: cons.all()){
          optionalValid &= c.valid(entity.getBuilding(ConsumerBuildComp.class));
        }
        if(optionalValid){
          optionalCurr = cons;

          if(!docons || (!cons.optionalAlwaysValid && !valid)) continue;
          for(BaseConsume c: cons.all()){
            c.update(entity.getBuilding(ConsumerBuildComp.class));
            if(c instanceof UncConsumePower) powerCons += ((UncConsumePower) c).usage;
          }

          float[] arr = optProgress.get(cons, () -> new float[]{0});
          arr[0] += 1/cons.craftTime*optionalCurr.delta(entity);
          while(arr[0] >= 1){
            arr[0] %= 1;
            triggerOpt(id);
          }
          cons.optionalDef.get(entity, cons);
          if(onlyOne) break;
        }
      }
    }
  }
  
  /**获取指定索引的消耗列表*/
  public BaseConsumers get(int index){
    return get().get(index);
  }
  
  /**获取指定索引处的可选消耗列表*/
  public BaseConsumers getOptional(int index){
    return index < getOptional().size()? getOptional().get(index) : null;
  }
  
  /**触发一次所有主要消耗项的trigger方法*/
  public void trigger(){
    if(current != null){
      for(BaseConsume cons: current.all()){
        cons.consume(entity.getBuilding(ConsumerBuildComp.class));
      }
      current.trigger.get(entity);
    }
  }
  
  /**触发一次所有可选消耗项的trigger方法*/
  public void triggerOpt(int id){
    if(getOptional() != null && getOptional().size() > id){
      BaseConsumers cons = getOptional().get(id);
      for(BaseConsume c: cons.all()){
        c.consume(entity.getBuilding(ConsumerBuildComp.class));
      }
      cons.trigger.get(entity);
    };
  }
  
  /**当前消耗列表除指定消耗项以外是否其他全部可用*/
  public boolean excludeValid(UncConsumeType type){
    boolean temp = true;
    for(BaseConsume cons: current.all()){
      if(cons.type() == type) continue;
      temp &= cons.valid(entity.getBuilding(ConsumerBuildComp.class));
    }
    return temp;
  }
  
  /**当前消耗列表是否可用*/
  public boolean valid(){
    return valid && entity.shouldConsume() && entity.getBuilding().enabled;
  }
  
  /**当前消耗列表指定消耗项是否可用*/
  public boolean valid(UncConsumeType type){
    return current.get(type) != null && current.get(type).valid(entity.getBuilding(ConsumerBuildComp.class));
  }
  
  /**制定的消耗列表是否可用*/
  public boolean valid(int index){
    if(index >= get().size()) return false;
    
    for(BaseConsume c: get().get(index).all()){
      if(!c.valid(entity.getBuilding(ConsumerBuildComp.class))) return false;
    }
    
    return true;
  }

  /**过滤器，将判断对当前选中的区域指定type下对输入的对象是否接受
  * 若可选过滤器已添加目标对象同样返回true
  * @param type 过滤器种类
  * @param target 通过过滤器的目标对象
  * @param acceptAll 是否接受所有清单的需求
   * @return 布尔值，是否接受此对象
  * */
  public boolean filter(UncConsumeType<?> type, Content target, boolean acceptAll){
    if(optionalFilter.containsKey(type) && optionalFilter.get(type).get(target.id)) return true;
    
    if(acceptAll) return allFilter.containsKey(type) && allFilter.get(type).get(target.id);
    
    return (entity.consumeCurrent() >= 0 && filter.get(entity.consumeCurrent()).containsKey(type)
        && filter.get(entity.consumeCurrent()).get(type).get(target.id));
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
