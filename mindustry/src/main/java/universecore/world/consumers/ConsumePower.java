package universecore.world.consumers;

import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.ctype.Content;
import mindustry.gen.Building;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;

/*仅仅保存消耗参数，能量消耗本身实际应用仍为默认consumes*/
public class ConsumePower<T extends Building & ConsumerBuildComp> extends BaseConsume<T>{
  public float usage;
  public float capacity;
  public boolean buffered;

  Seq<ConsumePower<T>> others = new Seq<>();

  public ConsumePower(float usage, float capacity){
    this.usage = usage;
    this.buffered = capacity > 0;
    this.capacity = capacity;
  }
  
  public ConsumeType<?> type(){
    return ConsumeType.power;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void merge(BaseConsume<T> other){
    if(other instanceof ConsumePower cons){
      buffered |= cons.buffered;
      capacity += cons.capacity;

      others.add(cons);
      return;
    }
    throw new IllegalArgumentException("only merge consume with same type");
  }

  public float requestedPower(T entity){
    float res = usage;
    for(ConsumePower<T> other: others){
      res += other.requestedPower(entity);
    }
    return res;
  }
  
  @Override
  public void build(T tile, Table table){}

  @Override
  public void update(T entity){}

  @Override
  public float efficiency(T entity){
    if(entity.power == null) return 0;
    if(buffered){
      return entity.power.status > 0? 1: 0;
    }
    else{
      return entity.power.status;
    }
  }

  @Override
  public void display(Stats stats) {
    stats.add(Stat.powerUse, usage * 60f, StatUnit.powerSecond);
  }

  @Override
  public void consume(T entity) {}
  
  @Override
  public Seq<Content> filter(){
    return null;
  }
}
