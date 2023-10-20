package universecore.world.consumers;

import arc.Core;
import arc.func.Boolp;
import arc.func.Floatp;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Scaling;
import mindustry.core.UI;
import mindustry.ctype.Content;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;

/*仅仅保存消耗参数，能量消耗本身实际应用仍为默认consumes*/
public class ConsumePower<T extends Building & ConsumerBuildComp> extends BaseConsume<T>{
  public float usage;
  public float capacity;
  public boolean buffered;

  public boolean showIcon;

  Seq<ConsumePower<T>> others = new Seq<>(ConsumePower.class);

  public ConsumePower(float usage, float capacity){
    this.usage = usage;
    this.buffered = capacity > 0;
    this.capacity = capacity;
  }
  
  public ConsumeType<?> type(){
    return ConsumeType.power;
  }

  @Override
  public boolean hasIcons() {
    return showIcon;
  }

  @Override
  public void buildIcons(Table table) {
    if (showIcon) {
      buildPowerImage(table, usage);
    }
  }

  public static void buildPowerImage(Table table, float usage) {
    table.stack(
        new Table(o -> {
          o.left();
          o.add(new Image(Icon.power)).size(32f).scaling(Scaling.fit);
        }),
        new Table(t -> {
          t.left().bottom();
          t.add(usage *60 >= 1000 ? UI.formatAmount((long) (usage *60)) + "/s" : (int)(usage*60) + "/s").style(Styles.outlineLabel);
          t.pack();
        })
    );
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
    for(ConsumePower<T> other: others.items){
      if (other == null) continue;
      res += other.requestedPower(entity);
    }
    return res;
  }

  @Override
  public void buildBars(T entity, Table bars) {
    Boolp buffered = () -> entity.block.consPower.buffered;
    Floatp capacity = () -> entity.block.consPower.capacity;
    bars.add(new Bar(
        () -> buffered.get() ? Core.bundle.format("bar.poweramount", Float.isNaN(entity.power.status*capacity.get())?
            "<ERROR>": (int)(entity.power.status*capacity.get())): Core.bundle.get("bar.power"),
        () -> Pal.powerBar,
        () -> Mathf.zero(entity.block.consPower.requestedPower(entity)) && entity.power.graph.getPowerProduced() + entity.power.graph.getBatteryStored() > 0f? 1f: entity.power.status)).growX();
    bars.row();
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
