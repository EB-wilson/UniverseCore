package universecore.components.blockcomp;

import arc.func.Cons2;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.world.Block;
import mindustry.world.meta.Stats;
import universecore.annotations.Annotations;
import universecore.annotations.Annotations.BindField;
import universecore.ui.table.RecipeTable;
import universecore.world.consumers.BaseConsumers;
import universecore.world.consumers.ConsFilter;
import universecore.world.consumers.ConsumePower;
import universecore.world.consumers.ConsumeType;
import universecore.world.meta.UncStat;

/**Consume组件，为方块添加可标记消耗项的功能
 *
 * @author EBwilson
 * @since 1.0*/
public interface ConsumerBlockComp{
  /**方块的消耗的清单列表*/
  @BindField(value = "consumers", initialize = "new arc.struct.Seq<>()")
  default Seq<BaseConsumers> consumers(){
    return null;
  }

  /**方块的可选消耗的清单列表*/
  @BindField(value = "optionalCons", initialize = "new arc.struct.Seq<>()")
  default Seq<BaseConsumers> optionalCons(){
    return null;
  }

  /**{@code getter-}方块的消耗列表ui布局表*/
  @BindField("recipeTable")
  default RecipeTable recipeTable(){
    return null;
  }

  /**{@code setter-}方块的消耗列表ui布局表*/
  @BindField("recipeTable")
  default void recipeTable(RecipeTable table){}


  /**{@code setter-}方块的可选消耗列表ui布局表*/
  @BindField("optionalRecipeTable")
  default RecipeTable optionalRecipeTable(){
    return null;
  }


  /**{@code setter-}方块的可选消耗列表ui布局表*/
  @BindField("optionalRecipeTable")
  default void optionalRecipeTable(RecipeTable table){}

  /**这个方块是否只在一次消耗可选列表时仅选中一个最靠前的可选项*/
  @BindField("oneOfOptionCons")
  default boolean oneOfOptionCons(){
    return false;
  }

  @BindField(value = "consFilter", initialize = "new universecore.world.consumers.ConsFilter()")
  default ConsFilter filter(){
    return null;
  }

  /**创建一个新的消耗列表插入容器，并返回它*/
  default BaseConsumers newConsume(){
    BaseConsumers consume = new BaseConsumers(false);
    consumers().add(consume);
    return consume;
  }

  /**创建一个可选消耗列表插入容器，并返回它
   *
   * @param validDef 当这个消耗项可用时每次刷新要进行的行为
   * @param displayDef 用于设置统计条目，显示该可选消耗的功能*/
  @SuppressWarnings("unchecked")
  default <T extends ConsumerBuildComp> BaseConsumers newOptionalConsume(Cons2<T, BaseConsumers> validDef, Cons2<Stats, BaseConsumers> displayDef){
    BaseConsumers consume = new BaseConsumers(true){{
        optionalDef = (Cons2<ConsumerBuildComp, BaseConsumers>) validDef;
        display = displayDef;
    }};
    optionalCons().add(consume);
    return consume;
  }
  
  /**为将方块加入到能量网络中，需要初始化一个原有的能量消耗器进行代理，这在组件化实现中由{@link Block#init()}入口调用*/
  @Annotations.MethodEntry(entryMethod = "init")
  default void initPower(){
    Block block = (Block)this;

    if(block.consumesPower){
      block.consumePowerDynamic(e -> {
        ConsumerBuildComp entity = (ConsumerBuildComp) e;
        if(entity.consumer().current == null) return 0f;
        if(entity.getBuilding().tile().build == null || entity.consumeCurrent() == -1 || !entity.consumer().excludeValid(ConsumeType.power))
          return 0f;

        ConsumePower<?> cons = entity.consumer().current.get(ConsumeType.power);
        if(cons == null) return 0;

        if(cons.buffered){
          return (1f - entity.getBuilding().power.status)*cons.capacity;
        }
        else{
          return entity.consumer().getPowerUsage()*Mathf.num(entity.shouldConsume());
        }
      });
    }
  }

  @Annotations.MethodEntry(entryMethod = "init")
  default void initFilter(){
    filter().applyFilter(consumers(), optionalCons());
    for(BaseConsumers consumer: consumers()){
      consumer.initFilter();
    }
  }

  @Annotations.MethodEntry(entryMethod = "setStats", context = "stats -> stats")
  default void setConsumeStats(Stats stats){
    if(consumers().size > 1){
      recipeTable(new RecipeTable(consumers().size));
      for(int i=0; i<consumers().size; i++){
        recipeTable().stats[i] = new Stats();
        consumers().get(i).display(recipeTable().stats[i]);
      }
      recipeTable().build();
    
      stats.add(UncStat.inputs, table -> {
        table.row();
        table.add(recipeTable()).grow();
      });
    }
    else if(consumers().size == 1){
      consumers().get(0).display(stats);
    }
  
    if(optionalCons().size > 0){
      optionalRecipeTable(new RecipeTable(optionalCons().size));
      for(int i=0; i<optionalCons().size; i++){
        optionalRecipeTable().stats[i] = new Stats();
        optionalCons().get(i).display(optionalRecipeTable().stats[i]);
      }
      optionalRecipeTable().build();
    
      stats.add(UncStat.optionalInputs, table -> {
        table.row();
        table.add(optionalRecipeTable()).grow();
      });
    }
  }
}
