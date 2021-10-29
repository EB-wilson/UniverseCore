package universeCore.world.consumers;

import arc.Core;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.ui.LiquidDisplay;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ConsumerBuildComp;
import universeCore.util.UncLiquidStack;

public class UncConsumeLiquids<T extends Building & ConsumerBuildComp> extends BaseConsume<T>{
  public boolean portion = false;
  public UncLiquidStack[] liquids;

  public UncConsumeLiquids(UncLiquidStack[] liquids){
    this.liquids = liquids;
  }
  
  public UncConsumeType<?> type(){
    return UncConsumeType.liquid;
  }
  
  public void portion(){
    this.portion = true;
  }
  
  @Override
  public void consume(T entity) {
    if(portion) for(UncLiquidStack stack: liquids){
      entity.liquids.remove(stack.liquid, stack.amount*60*entity.consumeMultiplier(this));
    }
  }

  @Override
  public void update(T entity) {
    if(!portion) for(UncLiquidStack stack: liquids){
      entity.liquids.remove(stack.liquid, stack.amount*entity.edelta()*entity.consumeMultiplier(this));
    }
  }

  @Override
  public void display(Stats stats) {
    stats.add(Stat.input, table -> {
      table.row();
      table.table(t -> {
        t.defaults().left().fill().padLeft(6);
        t.add(Core.bundle.get("misc.liquid") + ":");
        for(UncLiquidStack stack: liquids){
          t.add(new LiquidDisplay(stack.liquid, stack.amount*60, true));
        }
      }).left().padLeft(5);
    });
  }

  @Override
  public void build(T entity, Table table) {
    for(UncLiquidStack stack : liquids){
      table.add(new ReqImage(stack.liquid.uiIcon,
      () -> entity.liquids != null && entity.liquids.get(stack.liquid) > stack.amount*entity.consDelta(parent) + 0.0001f)).padRight(8);
    }
    table.row();
  }

  @Override
  public boolean valid(T entity){
    for(UncLiquidStack stack: liquids){
      if(entity.liquids == null || entity.liquids.get(stack.liquid) < stack.amount*(entity.block.hasPower && entity.power.status != 0? entity.consDelta(parent): entity.delta())) return false;
    }
    return true;
  }
  
  @Override
  public Liquid[] filter(T entity){
    int i = 0;
    Liquid[] acceptLiquids = new Liquid[liquids.length];
    for(UncLiquidStack stack: liquids){
      acceptLiquids[i++] = stack.liquid;
    }
    return acceptLiquids;
  }
}
