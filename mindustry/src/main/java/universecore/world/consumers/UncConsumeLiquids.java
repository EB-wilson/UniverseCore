package universecore.world.consumers;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import arc.struct.ObjectMap;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.ui.LiquidDisplay;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;
import universecore.util.UncLiquidStack;

public class UncConsumeLiquids<T extends Building & ConsumerBuildComp> extends BaseConsume<T>{
  private static final ObjectMap<Liquid, UncLiquidStack> TMP = new ObjectMap<>();

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
  public void merge(BaseConsume<T> other){
    if(other instanceof UncConsumeLiquids cons){
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
    throw new IllegalArgumentException("only merge consume with same type");
  }
  
  @Override
  public void consume(T entity) {
    if(portion) for(UncLiquidStack stack: liquids){
      entity.liquids.remove(stack.liquid, stack.amount*60*multiple(entity));
    }
  }

  @Override
  public void update(T entity) {
    if(!portion) for(UncLiquidStack stack: liquids){
      entity.liquids.remove(stack.liquid, stack.amount*parent.delta(entity)*multiple(entity));
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
      () -> entity.liquids != null && entity.liquids.get(stack.liquid) > (entity.efficiency() > 0.001f?
          stack.amount*parent.delta(entity)*multiple(entity) + 0.001f:
          stack.amount*multiple(entity)*Time.delta))).padRight(8);
    }
    table.row();
  }

  @Override
  public boolean valid(T entity){
    if(entity.liquids == null) return false;
    for(UncLiquidStack stack: liquids){
      if(entity.liquids == null || entity.liquids.get(stack.liquid) < (entity.efficiency() <= 0.001f?
          stack.amount*multiple(entity)*Time.delta:
          stack.amount*parent.delta(entity)*multiple(entity))) return false;
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
