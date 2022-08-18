package universecore.components.blockcomp;

import arc.func.Cons2;
import arc.math.Mathf;
import mindustry.world.Block;
import mindustry.world.meta.Stats;
import universecore.annotations.Annotations;
import universecore.annotations.Annotations.BindField;
import universecore.ui.table.RecipeTable;
import universecore.world.consumers.BaseConsumers;
import universecore.world.consumers.UncConsumeType;
import universecore.world.meta.UncStat;

import java.util.ArrayList;

/**Consume组件，为方块添加可标记消耗项的功能
 * @author EBwilson
 * @since 1.0*/
public interface ConsumerBlockComp{
  @BindField("consumers")
  default ArrayList<BaseConsumers> consumers(){
    return null;
  }
  
  @BindField("recipeTable")
  default RecipeTable recipeTable(){
    return null;
  }
  
  @BindField("recipeTable")
  default void recipeTable(RecipeTable table){}
  
  @BindField("optionalCons")
  default ArrayList<BaseConsumers> optionalCons(){
    return null;
  }
  
  @BindField("optionalRecipeTable")
  default RecipeTable optionalRecipeTable(){
    return null;
  }
  
  @BindField("optionalRecipeTable")
  default void optionalRecipeTable(RecipeTable table){}
  
  @BindField("oneOfOptionCons")
  default boolean oneOfOptionCons(){
    return false;
  }
  
  default BaseConsumers newConsume(){
    BaseConsumers consume = new BaseConsumers(false);
    consumers().add(consume);
    return consume;
  }
  
  default BaseConsumers newOptionalConsume(Cons2<ConsumerBuildComp, BaseConsumers> validDef, Cons2<Stats, BaseConsumers> displayDef){
    BaseConsumers consume = new BaseConsumers(true){
      {
        optionalDef = validDef;
        display = displayDef;
      }
    };
    optionalCons().add(consume);
    return consume;
  }
  
  /**为将方块加入到能量网络中，需要初始化一个原有的能量消耗器进行代理，在此进行init之前调用*/
  @Annotations.MethodEntry(entryMethod = "init", context = "powerCapacity -> powerCapacity")
  default void initPower(float powerCapacity){
    Block block = (Block)this;
    block.consumePowerDynamic(e -> {
      ConsumerBuildComp entity = (ConsumerBuildComp)e;
      if(entity.consumer().current == null) return 0f;
      if(entity.getBuilding().tile().build == null || entity.consumeCurrent() == -1 || !entity.consumer().excludeValid(UncConsumeType.power)) return 0f;
      if(powerCapacity > 0){
        return (1f-entity.getBuilding().power.status)*powerCapacity;
      }
      else{
        return entity.consumer().getPowerUsage() * Mathf.num(entity.shouldConsume());
      }
    });
  }

  @Annotations.MethodEntry(entryMethod = "setStats", context = "stats -> stats")
  default void setConsumeStats(Stats stats){
    if(consumers().size() > 1){
      recipeTable(new RecipeTable(consumers().size()));
      for(int i=0; i<consumers().size(); i++){
        recipeTable().stats[i] = new Stats();
        consumers().get(i).display(recipeTable().stats[i]);
      }
      recipeTable().build();
    
      stats.add(UncStat.inputs, table -> {
        table.row();
        table.add(recipeTable()).grow();
      });
    }
    else if(consumers().size() == 1){
      consumers().get(0).display(stats);
    }
  
    if(optionalCons().size() > 0){
      optionalRecipeTable(new RecipeTable(optionalCons().size()));
      for(int i=0; i<optionalCons().size(); i++){
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
