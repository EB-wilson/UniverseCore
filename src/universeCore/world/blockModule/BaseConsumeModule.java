package universeCore.world.blockModule;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.meta.BlockStatus;
import mindustry.world.modules.ConsumeModule;
import universeCore.entityComps.blockComps.ConsumerBuildComp;
import universeCore.world.consumers.BaseConsume;
import universeCore.world.consumers.BaseConsumers;
import universeCore.world.consumers.UncConsumeType;

import java.util.ArrayList;

public class BaseConsumeModule extends ConsumeModule{
  protected final ConsumerBuildComp entity;
  protected final BaseConsumers[] consumes;
  protected final BaseConsumers[] optionalCons;
  public final boolean oneOfOptionCons;
  public BaseConsumers current;
  public BaseConsumers optionalCurr;
  public boolean valid;
  public Seq<Seq<Seq<Object>>> filter = new Seq<>();

  public BaseConsumeModule(ConsumerBuildComp entity, ArrayList<BaseConsumers> cons, ArrayList<BaseConsumers> optional){
    super(entity.getBuilding());
    this.entity = entity;
    this.oneOfOptionCons = entity.getConsumerBlock().oneOfOptionCons();
    consumes = cons.size() > 0? cons.toArray(new BaseConsumers[0]): null;
    optionalCons = optional.size() > 0? optional.toArray(new BaseConsumers[0]): null;
    current = consumes != null && entity.consumeCurrent() != -1 ? consumes[entity.consumeCurrent()] : null;
    appliedFilter();
  }

  public void build(Table table){
    if(current != null) for(BaseConsume cons: current.all()){
      cons.build(entity, table);
    }
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
  
  public void appliedFilter(){
    /*数组向后移位，长度最少为1，[0]用于存储可选的消耗输入*/
    filter.add(new Seq<>());
    
    /*可选输入占用[0]*/
    if(optionalCons != null){
      for(BaseConsumers cons: optionalCons){
        for(BaseConsume c: cons.all()){
          if(c.filter(entity) != null){
            if(c.type().id() >= filter.get(0).size){
              filter.get(0).setSize(c.type().id()+1);
            }
            if(filter.get(0).get(c.type().id()) == null){
              filter.get(0).set(c.type().id(), new Seq<>());
            }
            filter.get(0).get(c.type().id()).addAll(c.filter(entity));
          }
        }
      }
    }
    
    /*从[1]开始*/
    int l = 1;
    if(consumes != null){
      for(BaseConsumers cons: consumes){
        filter.add(new Seq<>());
        for(BaseConsume c: cons.all()){
          if(c.filter(entity) != null){
            if(c.type().id() >= filter.get(l).size) filter.get(l).setSize(c.type().id() + 1);
            filter.get(l).set(c.type().id(), new Seq<>(c.filter(entity)));
          }
        }
        l++;
      }
    }
  }
  
  @Override
  public void update(){
    current = null;
    if((!hasOptional() && !hasConsume()) || entity.consumeCurrent() == -1) return;
    boolean docons = entity.shouldConsume() && entity.productionValid();
    valid = true;
    //Log.info("on consume update,data:[recipeCurrent:" + entity.recipeCurrent + ",consume:" + Arrays.toString(consumes) + ",optionalCons:" + Arrays.toString(optionalCons) + "]");
    if(entity.consumeCurrent() >= 0 && consumes != null){
      current = consumes[entity.consumeCurrent()];
      if(current != null) for(BaseConsume cons: current.all()){
        valid &= cons.valid(entity);
        if(docons && cons.valid(entity)){
          cons.update(entity);
        }
      }
    }
    if(optionalCons != null){
      for(BaseConsumers cons: optionalCons){
        optionalCurr = cons;
        boolean optionalValid = true;
        for(BaseConsume c: cons.all()){
          optionalValid &= c.valid(entity);
        }
        if(optionalValid){
          for(BaseConsume c: cons.all()){
            if(docons && c.valid(entity)) c.update(entity);
          }
          cons.method.get(entity.getBuilding(), cons);
          if(oneOfOptionCons){
            break;
          }
        }
      }
    }
  }

  public void trigger(){
    if(current != null) for(BaseConsume cons: current.all()){
      cons.consume(entity);
    }
    if(optionalCons != null){
      for(BaseConsumers cons: optionalCons){
        boolean optionalValid = true;
        for(BaseConsume c: cons.all()) optionalValid &= c.valid(entity);
        if(optionalValid){
          for(BaseConsume c: cons.all()){
            c.consume(entity);
          }
          if(oneOfOptionCons) break;
        }
      }
    }
  }
  
  public boolean excludeValid(int id){
    boolean temp = true;
    for(BaseConsume cons: current.all()){
      if(cons.type().id() == id) continue;
      temp &= cons.valid(entity);
    }
    return temp;
  }
  
  public boolean valid(){
    //Log.info("getField cons valid, data:[valid:" + valid + ",shouldConsume:" + entity.shouldConsume() + "enable:" + entity.enabled + "]");
    return valid && entity.shouldConsume() && entity.getBuilding().enabled;
  }
  
  public boolean valid(UncConsumeType<?, ?> type){
    return current.get(type) != null && current.get(type).valid(entity);
  }

  /**过滤器，将判断对当前选中的区域指定type下对输入的对象是否接受
  * 若可选过滤器已添加目标对象同样返回true
  * @param type 过滤器种类
  * @param target 通过过滤器的目标对象
  * @return 布尔值，是否接受此对象
  * */
  public boolean filter(UncConsumeType<?, ?> type, Object target){
    return (entity.consumeCurrent() >= 0 && filter.size > 1 && filter.get(entity.consumeCurrent() + 1).size > type.id() &&
      filter.get(entity.consumeCurrent() + 1).get(type.id()) != null && filter.get(entity.consumeCurrent() + 1).get(type.id()).contains(target)) ||
      filter.size > 0 && filter.get(0).size > type.id() && filter.get(0).get(type.id()) != null && filter.get(0).get(type.id()).contains(target)/*可选输入*/;
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
