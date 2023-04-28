package universecore.components.blockcomp;

import arc.scene.ui.layout.Table;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import mindustry.graphics.Pal;
import mindustry.world.meta.*;
import universecore.annotations.Annotations;
import universecore.world.consumers.BaseConsume;
import universecore.world.consumers.BaseConsumers;
import universecore.world.producers.BaseProduce;
import universecore.world.producers.BaseProducers;

/**工厂方块组件，描述{@linkplain FactoryBuildComp 工厂建筑}中必要的一些属性
 *
 * @since 1.4
 * @author EBwilson*/
public interface FactoryBlockComp extends ProducerBlockComp{
  /**方块的热机效率，由0-1的插值，为方块从启动到最大效率的速度，这是{@link arc.math.Mathf#lerpDelta(float, float, float)}的插值*/
  @Annotations.BindField("warmupSpeed")
  default float warmupSpeed(){
    return 0;
  }

  /**方块的冷却速度，由0-1的插值，为方块完全停机的速度，这是{@link arc.math.Mathf#lerpDelta(float, float, float)}的插值*/
  @Annotations.BindField("stopSpeed")
  default float stopSpeed(){
    return 0;
  }

  static void buildRecipe(Table table, BaseConsumers consumers, BaseProducers producers){
    Stats stats = new Stats();

    if (consumers != null){
      consumers.display(stats);
    }
    if (producers != null) {
      producers.display(stats);
    }

    buildStatTable(table, stats);
  }

  static void buildStatTable(Table table, Stats stat){
    for(StatCat cat : stat.toMap().keys()){
      OrderedMap<Stat, Seq<StatValue>> map = stat.toMap().get(cat);
      if(map.size == 0) continue;

      if(stat.useCategories){
        table.add("@category." + cat.name).color(Pal.accent).fillX();
        table.row();
      }

      for(Stat state : map.keys()){
        table.table(inset -> {
          inset.left();
          inset.add("[lightgray]" + state.localized() + ":[] ").left();
          Seq<StatValue> arr = map.get(state);
          for(StatValue value : arr){
            value.display(inset);
            inset.add().size(10f);
          }
        }).fillX().padLeft(10);
        table.row();
      }
    }
  }
}
