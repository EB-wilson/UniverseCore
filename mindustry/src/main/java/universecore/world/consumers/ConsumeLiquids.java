package universecore.world.consumers;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.ctype.Content;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.ui.LiquidDisplay;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;
import universecore.util.UncLiquidStack;

public class ConsumeLiquids<T extends Building & ConsumerBuildComp> extends ConsumeLiquidBase<T>{
  private static final ObjectMap<Liquid, UncLiquidStack> TMP = new ObjectMap<>();

  public boolean portion = false;

  public ConsumeLiquids(UncLiquidStack[] liquids){
    this.consLiquids = liquids;
  }
  
  public ConsumeType<?> type(){
    return ConsumeType.liquid;
  }
  
  @Override
  public TextureRegion icon(){
    return consLiquids[0].liquid.uiIcon;
  }
  
  public void portion(){
    this.portion = true;
  }

  @SuppressWarnings({"rawtypes", "DuplicatedCode"})
  @Override
  public void merge(BaseConsume<T> other){
    if(other instanceof ConsumeLiquids cons){
      TMP.clear();
      for(UncLiquidStack stack: consLiquids){
        TMP.put(stack.liquid, stack);
      }

      for(UncLiquidStack stack: cons.consLiquids){
        TMP.get(stack.liquid, () -> new UncLiquidStack(stack.liquid, 0)).amount += stack.amount;
      }

      consLiquids = TMP.values().toSeq().sort((a, b) -> a.liquid.id - b.liquid.id).toArray(UncLiquidStack.class);
      return;
    }
    throw new IllegalArgumentException("only merge consume with same type");
  }
  
  @Override
  public void consume(T entity) {
    if(portion) for(UncLiquidStack stack: consLiquids){
      entity.liquids.remove(stack.liquid, stack.amount*60*multiple(entity));
    }
  }

  @Override
  public void update(T entity) {
    if(!portion) for(UncLiquidStack stack: consLiquids){
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
        for(UncLiquidStack stack: consLiquids){
          t.add(new LiquidDisplay(stack.liquid, stack.amount*60, true));
        }
      }).left().padLeft(5);
    });
  }

  @Override
  public void build(T entity, Table table) {
    for(UncLiquidStack stack : consLiquids){
      table.add(new ReqImage(stack.liquid.uiIcon,
      () -> entity.liquids != null && entity.liquids.get(stack.liquid) > 0)).padRight(8);
    }
    table.row();
  }

  @Override
  public float efficiency(T entity){
    if(entity.liquids == null) return 0;
    if(portion){
      for(UncLiquidStack stack: consLiquids){
        if(entity.liquids.get(stack.liquid) < stack.amount*multiple(entity)*60) return 0;
      }
      return 1;
    }
    else{
      float min = 1;

      for(UncLiquidStack stack: consLiquids){
        min = Math.min(entity.liquids.get(stack.liquid)/(stack.amount*multiple(entity)), min);
      }

      if(min < 0.0001f) return 0;
      return Mathf.clamp(min);
    }
  }

  @Override
  public Seq<Content> filter(){
    return Seq.with(consLiquids).map(s -> s.liquid);
  }
}
