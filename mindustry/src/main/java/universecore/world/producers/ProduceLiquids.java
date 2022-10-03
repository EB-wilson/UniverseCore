package universecore.world.producers;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.ui.LiquidDisplay;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ProducerBuildComp;
import universecore.util.UncLiquidStack;

public class ProduceLiquids<T extends Building & ProducerBuildComp> extends BaseProduce<T>{
  private static final ObjectMap<Liquid, UncLiquidStack> TMP = new ObjectMap<>();

  public boolean shouldFill = true;
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
  public Color color(){
    return liquids[0].liquid.color;
  }

  @Override
  public TextureRegion icon(){
    return liquids[0].liquid.uiIcon;
  }

  @Override
  public void merge(BaseProduce<T> other){
    if(other instanceof ProduceLiquids cons){
      TMP.clear();
      for(UncLiquidStack stack: liquids){
        TMP.put(stack.liquid, stack);
      }

      for(UncLiquidStack stack: cons.liquids){
        TMP.get(stack.liquid, () -> new UncLiquidStack(stack.liquid, 0)).amount += stack.amount;
      }

      liquids = TMP.values().toSeq().sort((a, b) -> a.liquid.id - b.liquid.id).toArray(UncLiquidStack.class);
      return;
    }
    throw new IllegalArgumentException("only merge production with same type");
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
      float amount = stack.amount*parent.cons.delta(entity)*multiple(entity);
      amount = Math.min(amount, entity.block.liquidCapacity - entity.liquids.get(stack.liquid));
      entity.liquids.add(stack.liquid, amount);
    }
  }
  
  @Override
  public void dump(T entity) {
    for(UncLiquidStack stack: liquids){
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
        for(UncLiquidStack stack: liquids){
          t.add(new LiquidDisplay(stack.liquid, stack.amount*60, true));
        }
      }).left().padLeft(5);
    });
  }
  
  @Override
  public boolean valid(T entity){
    if(entity.liquids == null) return false;

    boolean res = false;
    for(UncLiquidStack stack: liquids){
      float mult = parent.cons.delta(entity)*multiple(entity);
      if(entity.liquids.get(stack.liquid) + stack.amount*mult > entity.block.liquidCapacity){
        if(!shouldFill) return false;
      }
      else res = true;
    }
    return res;
  }
}
