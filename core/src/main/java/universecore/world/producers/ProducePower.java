package universecore.world.producers;

import arc.Core;
import arc.func.Floatp;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ProducerBuildComp;
import universecore.world.consumers.ConsumePower;
import universecore.world.consumers.ConsumeType;

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

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public void buildBars(T entity, Table bars) {
    Floatp prod = () -> entity.powerProdEfficiency()*entity.producer().current.get(ProduceType.power).powerProduction;
    Floatp cons = () -> {
      ConsumePower cp;
      return entity.block.consumesPower && entity.consumer().current != null
          && (cp = entity.consumer().current.get(ConsumeType.power)) != null?
              cp.usage*cp.multiple(entity): 0;
    };
    bars.add(new Bar(
        () -> Core.bundle.format("bar.poweroutput", Strings.fixed(Math.max(prod.get() - cons.get(), 0)*60*entity.timeScale(), 1)),
        () -> Pal.powerBar,
        entity::powerProdEfficiency
    )).growX();
    bars.row();
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
