package universeCore.world.blockModule;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.modules.BlockModule;
import universeCore.entityComps.blockComps.*;
import universeCore.world.producers.*;

import java.util.List;

@SuppressWarnings("all")
public class BaseProductModule extends BlockModule {
  public BaseConsumeModule consumer;
  
  protected final ProducerBuildComp entity;
  protected final BaseProducers[] produces;
  public BaseProducers current;
  public boolean valid;
  
  public BaseProductModule(ProducerBuildComp entity, List<BaseProducers> producers){
    this.entity = entity;
    consumer = entity.consumer();
    this.produces = new BaseProducers[producers.size()];
    for(int i=0; i<produces.length; i++){
      produces[i] = producers.get(i);
    }
    current = entity.produceCurrent() != -1? produces[entity.produceCurrent()]: null;
  }
  
  public void trigger(){
    if(current != null) for(BaseProduce prod: current.all()){
      prod.produce(entity.getBuilding());
    }
  }
  
  public void setCurrent(){
    current = produces[entity.consumeCurrent()];
  }
  
  public void update(){
    current = null;
    if(entity.produceCurrent() == -1 || produces == null) return;
    setCurrent();
    boolean docons = entity.consValid() && entity.shouldConsume() && entity.productionValid();
    boolean preValid = valid();
    
    valid = true;
    if(current != null) for(BaseProduce prod: current.all()){
      valid &= prod.valid(entity.getBuilding());
      if(docons && preValid && prod.valid(entity.getBuilding())){
        prod.update(entity.getBuilding());
        //Log.info("Run update,recipeCurrent:" + prod.id());
      }
    }
    //Log.info("update is run，docons:" + docons + ",valid:" + valid);
    for(BaseProducers p: produces){
      for(BaseProduce prod: p.all()) prod.dump(entity.getBuilding());
    }
  }
  
  public int indexOf(BaseProducers prod){
    for(int i=0; i<produces.length; i++){
      if(produces[i] == prod) return i;
    }
    return -1;
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
