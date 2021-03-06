package universecore.world.blocks.modules;

import arc.math.Mathf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.modules.BlockModule;
import universecore.components.blockcomp.ProducerBuildComp;
import universecore.world.producers.BaseProduce;
import universecore.world.producers.BaseProducers;
import universecore.world.producers.ProduceType;

import java.util.ArrayList;

/**生产者的产出模块，用于集中处理方块的生产工作
 * @author EBwilson*/
@SuppressWarnings("all")
public class BaseProductModule extends BlockModule {
  public BaseConsumeModule consumer;
  
  public final ProducerBuildComp entity;
  public BaseProducers current;
  public boolean valid;
  
  public BaseProductModule(ProducerBuildComp entity){
    this.entity = entity;
    consumer = entity.consumer();
    current = entity.produceCurrent() != -1? entity.getProducerBlock().producers().get(entity.produceCurrent()) : null;
  }

  public ArrayList<BaseProducers> get(){
    return entity.getProducerBlock().producers();
  }
  
  public void trigger(){
    if(current != null) for(BaseProduce prod: current.all()){
      prod.produce(entity.getBuilding(ProducerBuildComp.class));
    }
  }

  public float getPowerProduct(){
    if(current == null) return 0;
    return current.get(ProduceType.power).powerProduction*(Mathf.num(entity.shouldConsume() && entity.consValid())*((BaseProduce<ProducerBuildComp>)current.get(ProduceType.power)).multiple(entity));
  }
  
  public void setCurrent(){
    current = get().get(entity.consumeCurrent());
  }
  
  public void update(){
    current = null;
    if(get() == null) return;
    
    //只在选择了生产列表时才进行产出更新
    if(entity.produceCurrent() >= 0){
      setCurrent();
      boolean docons = entity.consValid() && entity.shouldConsume() && entity.productionValid();
      boolean preValid = valid();
  
      valid = true;
      if(current != null) for(BaseProduce prod : current.all()){
        valid &= prod.valid(entity.getBuilding(ProducerBuildComp.class));
        if(docons && preValid && prod.valid(entity.getBuilding(ProducerBuildComp.class))){
          prod.update(entity.getBuilding(ProducerBuildComp.class));
        }
      }
    }
    
    //无论何时都向外导出产品
    doDump(entity);
  }
  
  public void doDump(ProducerBuildComp entity){
    for(BaseProducers p: get()){
      for(BaseProduce prod: p.all()) prod.dump(entity.getBuilding(ProducerBuildComp.class));
    }
  }
  
  public boolean valid(){
    return valid && entity.productionValid() && entity.getBuilding().enabled;
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
