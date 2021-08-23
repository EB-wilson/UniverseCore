package universeCore.world.blockModule;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.modules.BlockModule;
import universeCore.entityComps.blockComps.ProducerBuildComp;
import universeCore.world.producers.BaseProduce;
import universeCore.world.producers.BaseProducers;

import java.util.List;

public class BaseProductModule extends BlockModule {
  protected final ProducerBuildComp entity;
  protected final BaseProducers[] produces;
  public BaseProducers current;
  public boolean valid;
  
  public BaseProductModule(ProducerBuildComp entity, List<BaseProducers> producers){
    this.entity = entity;
    this.produces = producers.toArray(new BaseProducers[0]);
    current = entity.produceCurrent() != -1? produces[entity.produceCurrent()]: null;
  }
  
  public void trigger(){
    if(current != null) for(BaseProduce prod: current.all()){
      prod.produce(entity);
    }
  }
  
  public void update(){
    current = null;
    if(entity.produceCurrent() == -1 || produces == null) return;
    current = produces[entity.produceCurrent()];
    boolean docons = entity.consValid() && entity.shouldConsume() && entity.productionValid();
    valid = true;
    if(current != null) for(BaseProduce prod: current.all()){
      valid &= prod.valid(entity);
      if(docons && prod.valid(entity)){
        prod.update(entity);
        //Log.info("Run update,recipeCurrent:" + prod.id());
      }
      prod.dump(entity);
    }
    //Log.info("update is run，docons:" + docons + ",valid:" + valid);
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
