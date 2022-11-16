package universecore.world.blocks.modules;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.modules.BlockModule;
import universecore.components.blockcomp.ProducerBuildComp;
import universecore.world.producers.BaseProduce;
import universecore.world.producers.BaseProducers;
import universecore.world.producers.ProduceType;

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

  public Seq<BaseProducers> get(){
    return entity.getProducerBlock().producers();
  }
  
  public void trigger(){
    if(current != null) for(BaseProduce prod: current.all()){
      prod.produce(entity.getBuilding(ProducerBuildComp.class));
    }
  }

  public float getPowerProduct(){
    if(current == null) return 0;
    return current.get(ProduceType.power).powerProduction*(Mathf.num(entity.shouldConsume() && entity.consumeValid())*((BaseProduce<ProducerBuildComp>)current.get(ProduceType.power)).multiple(entity));
  }
  
  public void setCurrent(){
    current = entity.consumeCurrent() == -1? null: get().get(entity.consumeCurrent());
  }
  
  public void update(){
    setCurrent();

    valid = true;
    //只在选择了生产列表时才进行产出更新
    if(current != null){
      setCurrent();
      boolean doprod = entity.consumeValid() && entity.shouldConsume() && entity.shouldProduct();
      boolean preValid = valid();

      boolean anyValid = false;
      for(BaseProduce prod : current.all()){
        boolean v = prod.valid(entity.getBuilding(ProducerBuildComp.class));
        anyValid |= v;
        valid &= !prod.shouldBlockWhenFull() || v;
        if(doprod && preValid && v){
          prod.update(entity.getBuilding(ProducerBuildComp.class));
        }
      }
      if(!anyValid) valid = false;
    }
    
    //无论何时都向外导出产品
    doDump(entity);
  }
  
  public void doDump(ProducerBuildComp entity){
    if(current != null){
      for(BaseProduce prod: current.all()){
        prod.dump(entity.getBuilding(ProducerBuildComp.class));
      }
    }
  }
  
  public boolean valid(){
    return valid && entity.getBuilding().enabled;
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
