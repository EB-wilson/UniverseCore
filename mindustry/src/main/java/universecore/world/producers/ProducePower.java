package universecore.world.producers;

import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.core.UI;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ProducerBuildComp;
import universecore.world.consumers.ConsumePower;

public class ProducePower<T extends Building & ProducerBuildComp> extends BaseProduce<T>{
  public float powerProduction;
  public boolean showIcon = true;
  
  public ProducePower(float prod){
    powerProduction = prod;
  }
  
  @Override
  public ProduceType<ProducePower<?>> type(){
    return ProduceType.power;
  }

  @Override
  public boolean hasIcons() {
    return showIcon;
  }

  @Override
  public void buildIcons(Table table) {
    if (showIcon){
      ConsumePower.buildPowerImage(table, powerProduction);
    }
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
