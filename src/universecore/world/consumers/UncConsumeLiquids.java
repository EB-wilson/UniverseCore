package universecore.world.consumers;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.ui.LiquidDisplay;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;
import universecore.util.UncLiquidStack;

public class UncConsumeLiquids<T extends Building & ConsumerBuildComp> extends BaseConsume<T>{
  public boolean portion = false;
  public UncLiquidStack[] liquids;

  public UncConsumeLiquids(UncLiquidStack[] liquids){
    this.liquids = liquids;
  }
  
  public UncConsumeType<?> type(){
    return UncConsumeType.liquid;
  }
  
  @Override
  public TextureRegion icon(){
    return liquids[0].liquid.uiIcon;
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
      if(entity.liquids == null || entity.liquids.get(stack.liquid) < stack.amount*(entity.block.hasPower && entity.power.status > 0? entity.delta()*entity.power.status: entity.delta())*entity.consumeMultiplier(this)) return false;
    }
    return true;
  }
  
  @Override
  public Bits filter(T entity){
    Bits result = new Bits(Vars.content.liquids().size);
    for(UncLiquidStack stack: liquids){
      result.set(stack.liquid.id);
    }
    return result;
  }
}
