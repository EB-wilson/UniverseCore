package universeCore.world.producers;

import arc.Core;
import mindustry.gen.Building;
import mindustry.ui.LiquidDisplay;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ProducerBuildComp;
import universeCore.util.UncLiquidStack;

public class ProduceLiquids<T extends Building & ProducerBuildComp> extends BaseProduce<T>{
  public boolean portion = false;
  public UncLiquidStack[] liquids;

  public ProduceLiquids(UncLiquidStack[] liquids){
    this.liquids = liquids;
  }
  
  public void Portion(){
    this.portion = true;
  }
  
  @Override
  public ProduceType<ProduceLiquids<?>> type(){
    return ProduceType.liquid;
  }
  
  @Override
  public void produce(T entity) {
    if(portion) for(UncLiquidStack stack: liquids){
      entity.liquids.add(stack.liquid, stack.amount*60);
    }
  }

  @Override
  public void update(T entity) {
    if(!portion) for(UncLiquidStack stack: liquids){
      entity.liquids.add(stack.liquid, stack.amount*entity.consDelta(parent)*entity.productMultiplier(this));
      //Log.info("Liquid update is running, output:：" + stack.amount*entity.edelta() + ",amount:" + stack.amount + ",efficiency:" + entity.efficiency() + ",delta:" + entity.delta());
    }
  }
  
  @Override
  public void dump(T entity) {
    for(UncLiquidStack stack: liquids){
      entity.dumpLiquid(stack.liquid);
    }
  }
  
  @Override
  public void display(Stats stats) {
    stats.add(Stat.output, table -> {
      table.row();
      table.table(t -> {
        t.defaults().left().fill().padLeft(6);
        t.add(Core.bundle.get("misc.liquid") + ":").left();
        for(UncLiquidStack stack: liquids){
          t.add(new LiquidDisplay(stack.liquid, stack.amount*60, true));
        }
      }).left().padLeft(5);
    });
  }
  
  @Override
  public boolean valid(T entity){
    for(UncLiquidStack stack: liquids){
      if(entity.liquids.get(stack.liquid) >= entity.block.liquidCapacity) return false;
    }
    return true;
  }
}
