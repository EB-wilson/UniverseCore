package universeCore.entityComps.blockComps;

import arc.func.Cons2;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.meta.Stats;
import universeCore.ui.table.RecipeTable;
import universeCore.util.handler.FieldHandler;
import universeCore.world.consumers.BaseConsumers;
import universeCore.world.consumers.UncConsumeType;
import universeCore.world.meta.UncStat;

import java.util.ArrayList;

/**Consume组件，为方块添加可标记消耗项的功能
 * @author EBwilson */
public interface ConsumerBlockComp{
  ArrayList<BaseConsumers> consumers();
  
  RecipeTable recipeTable();
  
  void recipeTable(RecipeTable table);
  
  ArrayList<BaseConsumers> optionalCons();
  
  RecipeTable optionalRecipeTable();
  
  void optionalRecipeTable(RecipeTable table);
  
  boolean oneOfOptionCons();
  
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
  
  /**为将方块加入到能量网络中，需要初始化一个原有的能量消耗器进行代理，在此进行，int之前调用*/
  default void initPower(float powerCapacity){
    Block block = (Block)this;
    block.consumes.add(new ConsumePower(0 ,powerCapacity, powerCapacity > 0){
      @Override
      public float requestedPower(Building e){
        ConsumerBuildComp entity = (ConsumerBuildComp)e;
        if(entity.consumer().current == null) return 0f;
        if(entity.getBuilding().tile().build == null || entity.consumeCurrent() == -1 || !entity.consumer().excludeValid(UncConsumeType.power)) return 0f;
        if(buffered){
          return (1f-entity.getBuilding().power.status)*capacity;
        }
        else{
          return entity.consumer().getPowerUsage() * Mathf.num(entity.shouldConsume());
        }
      }
    });
  }
  
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
