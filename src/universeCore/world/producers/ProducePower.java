package universeCore.world.producers;

import mindustry.gen.Building;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ProducerBuildComp;

public class ProducePower<T extends Building & ProducerBuildComp> extends BaseProduce<T>{
  public float powerProduction;
  public final Stat generationType;
  
  public ProducePower(float prod){
    powerProduction = prod;
    generationType = Stat.basePowerGeneration;
  }
  
  public ProducePower(float prod, Stat type){
    powerProduction = prod;
    generationType = type;
  }
  
  @Override
  public ProduceType<ProducePower<?>> type(){
    return ProduceType.power;
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
    stats.add(generationType, powerProduction * 60.0f, StatUnit.powerSecond);
  }
}
