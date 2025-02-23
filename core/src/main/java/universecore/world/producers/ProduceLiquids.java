package universecore.world.producers;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValues;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ProducerBuildComp;
import universecore.world.consumers.ConsumeLiquidBase;

public class ProduceLiquids<T extends Building & ProducerBuildComp> extends BaseProduce<T>{
  private static final ObjectMap<Liquid, LiquidStack> TMP = new ObjectMap<>();

  public int displayLim = 4;
  public boolean portion = false;
  public LiquidStack[] liquids;

  public ProduceLiquids(LiquidStack[] liquids){
    this.liquids = liquids;
  }
  
  public ProduceLiquids<T> portion(){
    this.portion = true;
    return this;
  }

  @Override
  public void buildBars(T entity, Table bars) {
    for (LiquidStack stack : liquids) {
      bars.add(new Bar(
          () -> stack.liquid.localizedName,
          () -> stack.liquid.barColor != null? stack.liquid.barColor: stack.liquid.color,
          () -> Math.min(entity.liquids.get(stack.liquid) / entity.block.liquidCapacity, 1f)
      ));
      bars.row();
    }
  }
  
  @Override
  public ProduceType<ProduceLiquids<?>> type(){
    return ProduceType.liquid;
  }

  @Override
  public Color color(){
    return liquids[0].liquid.color;
  }

  @Override
  public void buildIcons(Table table) {
    ConsumeLiquidBase.buildLiquidIcons(table, liquids, false, displayLim);
  }

  @Override
  public void merge(BaseProduce<T> other){
    if(other instanceof ProduceLiquids cons){
      TMP.clear();
      for(LiquidStack stack: liquids){
        TMP.put(stack.liquid, stack);
      }

      for(LiquidStack stack: cons.liquids){
        TMP.get(stack.liquid, () -> new LiquidStack(stack.liquid, 0)).amount += stack.amount;
      }

      liquids = TMP.values().toSeq().sort((a, b) -> a.liquid.id - b.liquid.id).toArray(LiquidStack.class);
      return;
    }
    throw new IllegalArgumentException("only merge production with same type");
  }
  
  @Override
  public void produce(T entity) {
    if(portion) for(LiquidStack stack: liquids){
      entity.handleLiquid(entity, stack.liquid, stack.amount*60);
    }
  }

  @Override
  public void update(T entity) {
    if(!portion) for(LiquidStack stack: liquids){
      float amount = stack.amount*parent.cons.delta(entity)*multiple(entity);
      amount = Math.min(amount, entity.block.liquidCapacity - entity.liquids.get(stack.liquid));
      entity.handleLiquid(entity, stack.liquid, amount);
    }
  }
  
  @Override
  public void dump(T entity) {
    for(LiquidStack stack: liquids){
      if(entity.liquids.get(stack.liquid) > 0.01f) entity.dumpLiquid(stack.liquid);
    }
  }
  
  @Override
  public void display(Stats stats) {
    stats.add(Stat.output, table -> {
      table.row();
      table.table(t -> {
        t.defaults().left().fill().padLeft(6);
        t.add(Core.bundle.get("misc.liquid") + ":").left();
        for(LiquidStack stack: liquids){
          t.add(StatValues.displayLiquid(stack.liquid, stack.amount*60, true));
        }
      }).left().padLeft(5);
    });
  }

  @Override
  public boolean valid(T entity){
    if(entity.liquids == null) return false;

    boolean res = false;
    for(LiquidStack stack: liquids){
      if(entity.liquids.get(stack.liquid) + stack.amount*multiple(entity) > entity.block.liquidCapacity - 0.001f){
        if(blockWhenFull) return false;
      }
      else res = true;
    }
    return res;
  }
}
