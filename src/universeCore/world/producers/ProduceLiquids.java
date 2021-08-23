package universeCore.world.producers;

import arc.Core;
import mindustry.gen.Building;
import mindustry.ui.LiquidDisplay;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ProducerBuildComp;
import universeCore.util.UncLiquidStack;

public class ProduceLiquids extends BaseProduce{
  public boolean portion = false;
  public UncLiquidStack[] liquids;

  public ProduceLiquids(UncLiquidStack[] liquids){
    this.liquids = liquids;
  }
  
  public void Portion(){
    this.portion = true;
  }
  
  @Override
  public ProduceType<ProduceLiquids, Building> type(){
    return ProduceType.liquid;
  }
  
  @Override
  public void produce(ProducerBuildComp entity) {
    if(portion) for(UncLiquidStack stack: liquids){
      entity.getBuilding().liquids.add(stack.liquid, stack.amount*60);
    }
  }

  @Override
  public void update(ProducerBuildComp entity) {
    if(!portion) for(UncLiquidStack stack: liquids){
      entity.getBuilding().liquids.add(stack.liquid, stack.amount*entity.getBuilding().edelta());
      //Log.info("Liquid update is running, output:：" + stack.amount*entity.edelta() + ",amount:" + stack.amount + ",efficiency:" + entity.efficiency() + ",delta:" + entity.delta());
    }
  }
  
  @Override
  public void dump(ProducerBuildComp entity) {
    for(UncLiquidStack stack: liquids){
      entity.getBuilding().dumpLiquid(stack.liquid);
    }
  }
  
  @Override
  public void display(Stats stats) {
    stats.add(Stat.output, table -> {
      table.row();
      table.defaults().left();
      table.add(Core.bundle.get("misc.liquid") + ":").left();
      for(UncLiquidStack stack: liquids){
        table.add(new LiquidDisplay(stack.liquid, stack.amount*60, true)).padRight(5);
      }
    });
  }
  
  @Override
  public boolean valid(ProducerBuildComp entity){
    for(UncLiquidStack stack: liquids){
      if(entity.getBuilding().liquids.get(stack.liquid) >= entity.getBlock().liquidCapacity) return false;
    }
    return true;
  }
}
