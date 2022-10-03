package universecore.world.producers;

import mindustry.gen.Building;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ProducerBuildComp;

public class ProducePower<T extends Building & ProducerBuildComp> extends BaseProduce<T>{
  public float powerProduction;
  
  public ProducePower(float prod){
    powerProduction = prod;
  }
  
  @Override
  public ProduceType<ProducePower<?>> type(){
    return ProduceType.power;
  }

  @Override
  public void merge(BaseProduce<T> other){
    if(other instanceof ProducePower cons){
      powerProduction += cons.powerProduction;
      return;
    }
    throw new IllegalArgumentException("only merge production with same type");
  }

  @Override
  public void produce(Building entity) {
    /*不在此更新能量生产*/
  }
  
  @Override
  public void update(Building entity) {
    /*此处不进行能量更新*/
  }
  
  @Override
  public void display(Stats stats) {
    stats.add(Stat.basePowerGeneration, powerProduction * 60.0f, StatUnit.powerSecond);
  }

  @Override
  public boolean valid(T entity){
    return entity.power != null;
  }
}
