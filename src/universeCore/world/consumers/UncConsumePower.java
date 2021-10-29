package universeCore.world.consumers;

import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.world.Build;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ConsumerBuildComp;

/*仅仅保存消耗参数，能量消耗本身实际应用仍为默认consumes*/
public class UncConsumePower<T extends Building & ConsumerBuildComp> extends BaseConsume<T>{
  public final float usage;
  public final boolean buffered;

  public UncConsumePower(float usage, boolean buffered){
    this.usage = usage;
    this.buffered = buffered;
  }
  
  public UncConsumeType<?> type(){
    return UncConsumeType.power;
  }
  
  @Override
  public void build(T tile, Table table){
    //No tooltip for power, for now
  }

  @Override
  public void update(T entity){

  }

  @Override
  public boolean valid(T entity){
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
  public void consume(T entity) {

  }

  @Override
  public Object[] filter(T entity) {
    return null;
  }
}
