package universeCore.world.consumers;

import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.world.Build;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ConsumerBuildComp;

/*仅仅保存消耗参数，能量消耗本身实际应用仍为默认consumes*/
public class UncConsumePower extends BaseConsume<Building>{
  /** The maximum amount of power which can be processed per tick. This might influence efficiency or load a buffer. */
  public final float usage;
  /** True if the module can store power. */
  public final boolean buffered;

  public UncConsumePower(float usage, boolean buffered){
    this.usage = usage;
    this.buffered = buffered;
  }
  
  public UncConsumeType<UncConsumePower> type(){
    return UncConsumeType.power;
  }
  
  @Override
  public void build(Building tile, Table table){
    //No tooltip for power, for now
  }

  @Override
  public void update(Building entity){

  }

  @Override
  public boolean valid(Building entity){
    if(buffered){
      return true;
    }
    else{
      return entity.power.status > 0f;
    }
  }

  @Override
  public void display(Stats stats) {
    stats.add(Stat.powerUse, usage * 60f, StatUnit.powerSecond);
  }

  @Override
  public void consume(Building entity) {

  }

  @Override
  public Object[] filter(Building entity) {
    return null;
  }
}
