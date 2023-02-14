package universecore.world.blocks.modules;

import arc.func.Boolf;
import arc.func.Cons;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ctype.Content;
import mindustry.world.meta.BlockStatus;
import mindustry.world.modules.BlockModule;
import universecore.components.blockcomp.ConsumerBlockComp;
import universecore.components.blockcomp.ConsumerBuildComp;
import universecore.world.consumers.BaseConsume;
import universecore.world.consumers.BaseConsumers;
import universecore.world.consumers.ConsumePower;
import universecore.world.consumers.ConsumeType;

/**生产者的消耗器模块，用于集中处理方块的材料需求等，提供了可选需求以及其特殊的触发器
 * @author EBwilson*/
@SuppressWarnings("all")
public class BaseConsumeModule extends BlockModule{
  private static final float[] ZERO = {0};

  protected final ConsumerBuildComp entity;
  protected final ObjectMap<BaseConsumers, float[]> optProgress = new ObjectMap<>();
  protected final ObjectMap<BaseConsumers, float[]> optEfficiency = new ObjectMap<>();

  public BaseConsumers current;
  public BaseConsumers optionalCurr;
  public boolean valid;

  public float consEfficiency;

  private float powerCons;

  public BaseConsumeModule(ConsumerBuildComp entity){
    this.entity = entity;
    current = entity.getConsumerBlock().consumers() != null && entity.consumeCurrent() != -1?
        entity.getConsumerBlock().consumers().get(entity.consumeCurrent()): null;
  }

  public void build(Table table){
    if(current != null) for(BaseConsume cons: current.all()){
      cons.build(entity.getBuilding(ConsumerBuildComp.class), table);
    }
  }
  
  public Seq<BaseConsumers> get(){
    return entity.getConsumerBlock().consumers();
  }
  
  public Seq<BaseConsumers> getOptional(){
    return entity.getConsumerBlock().optionalCons();
  }
  
  public BlockStatus status(){
    if(!entity.shouldConsume()){
      return BlockStatus.noOutput;
    }

    if(!valid){
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

  /**@deprecated 1.6.2 不再使用，保留API避免异常*/
  @Deprecated
  public void applyFilter(){
    //noaction
  }

  public float getOptionalEff(BaseConsumers consumers){
    return optEfficiency.get(consumers, ZERO)[0];
  }

  public float getPowerUsage(){
    return powerCons*((BaseConsume<ConsumerBuildComp>)current.get(ConsumeType.power)).multiple(entity);
  }
  
  public void setCurrent(){
    current = entity.consumeCurrent() == -1? null: get().get(entity.consumeCurrent());
  }

  public void update(){
    setCurrent();
    powerCons = 0;
    if((!hasOptional() && !hasConsume())) return;

    valid = true;
    
    //只在选中消耗列表时才进行消耗更新
    if(current != null){
      boolean preValid = valid();

      for(Boolf<ConsumerBuildComp> b: current.valid){
        valid &= b.get(entity);
      }
      consEfficiency = valid? 1: 0;
      for(BaseConsume cons: current.all()){
        if(cons instanceof ConsumePower) powerCons += ((ConsumePower) cons).requestedPower(entity.getBuild());
        float eff = cons.efficiency(entity.getBuilding(ConsumerBuildComp.class));
        consEfficiency *= eff;
        valid &= eff > 0.0001f;

        if(!valid){
          consEfficiency = 0;
          break;
        }

        if(preValid && entity.shouldConsume()){
          cons.update(entity.getBuilding(ConsumerBuildComp.class));
        }
      }
    }
    
    //更新可选消耗列表
    if(getOptional() != null){
      BaseConsumers cons;
      boolean onlyOne = entity.getConsumerBlock().oneOfOptionCons();
      for(int id=0; id<getOptional().size; id++){
        cons = getOptional().get(id);
        
        float optionalEff = 1;
        for(Boolf<ConsumerBuildComp> b: cons.valid){
          optionalEff *= b.get(entity)? 1: 0;
        }
        for(BaseConsume c: cons.all()){
          optionalEff *= c.efficiency(entity.getBuilding(ConsumerBuildComp.class));
        }
        optEfficiency.get(cons, () -> new float[1])[0] = optionalEff;

        if(optionalEff > 0.0001f){
          optionalCurr = cons;

          if(!entity.shouldConsumeOptions() || (!cons.optionalAlwaysValid && !valid)) continue;
          for(BaseConsume c: cons.all()){
            c.update(entity.getBuilding(ConsumerBuildComp.class));
            if(c instanceof ConsumePower) powerCons += ((ConsumePower) c).requestedPower(entity.getBuild());
          }

          if(cons.craftTime > 0){
            float[] arr = optProgress.get(cons, () -> new float[]{0});
            arr[0] += 1/cons.craftTime*cons.delta(entity);
            while(arr[0] >= 1){
              arr[0] %= 1;
              triggerOpt(id);
            }
          }

          cons.optionalDef.get(entity, cons);
          if(onlyOne) break;
        }
      }
    }
  }

  public float consDelta(){
    return current == null? 0: current.delta(entity);
  }

  /**获取指定索引的消耗列表*/
  public BaseConsumers get(int index){
    return get().get(index);
  }
  
  /**获取指定索引处的可选消耗列表*/
  public BaseConsumers getOptional(int index){
    return index < getOptional().size? getOptional().get(index) : null;
  }
  
  /**触发当前主要消耗项的trigger方法*/
  public void trigger(){
    if(current != null){
      for(BaseConsume cons: current.all()){
        cons.consume(entity.getBuilding(ConsumerBuildComp.class));
      }
      for(Cons<ConsumerBuildComp> trigger: current.triggers){
        trigger.get(entity);
      }
    }
  }
  
  /**触发一个可选消耗项的trigger方法*/
  public void triggerOpt(int id){
    if(getOptional() != null && getOptional().size > id){
      BaseConsumers cons = getOptional().get(id);
      for(BaseConsume c: cons.all()){
        c.consume(entity.getBuilding(ConsumerBuildComp.class));
      }
      for(Cons<ConsumerBuildComp> trigger: cons.triggers){
        trigger.get(entity);
      }
    };
  }
  
  /**当前消耗列表除指定消耗项以外是否其他全部可用*/
  public boolean excludeValid(ConsumeType type){
    boolean temp = true;
    for(BaseConsume cons: current.all()){
      if(cons.type() == type) continue;
      temp &= cons.efficiency(entity.getBuilding(ConsumerBuildComp.class)) > 0.0001f;
    }
    return temp;
  }
  
  /**当前消耗列表是否可用*/
  public boolean valid(){
    return valid && entity.getBuilding().enabled;
  }
  
  /**当前消耗列表指定消耗项是否可用*/
  public boolean valid(ConsumeType type){
    return current.get(type) != null && current.get(type).efficiency(entity.getBuilding(ConsumerBuildComp.class)) > 0.0001;
  }
  
  /**指定的消耗列表是否可用*/
  public boolean valid(int index){
    if(index >= get().size) return false;
    
    for(BaseConsume c: get().get(index).all()){
      if(c.efficiency(entity.getBuilding(ConsumerBuildComp.class)) < 0.0001f) return false;
    }
    
    return true;
  }

  /**过滤器，将判断对当前选中的区域指定type下对输入的对象是否接受
   * 若可选过滤器已添加目标对象同样返回true
   * @param type 过滤器种类
   * @param target 通过过滤器的目标对象
   * @param acceptAll 是否接受所有清单的需求
   * @return 布尔值，是否接受此对象
   *
   * @deprecated 1.6.2 过滤器已重新优化以减少内存占用，此方法已废弃，请参阅{@link universecore.world.consumers.ConsFilter}
   **/
  @Deprecated
  public boolean filter(ConsumeType<?> type, Content target, boolean acceptAll){
    return entity.getBlock(ConsumerBlockComp.class).filter().filter(entity, type, target, acceptAll);
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
