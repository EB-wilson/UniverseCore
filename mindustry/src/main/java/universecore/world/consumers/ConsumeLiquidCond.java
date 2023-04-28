package universecore.world.consumers;

import arc.Core;
import arc.func.Boolf;
import arc.func.Floatf;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.LiquidDisplay;
import mindustry.ui.MultiReqImage;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;

import static mindustry.Vars.content;

public class ConsumeLiquidCond<T extends Building & ConsumerBuildComp> extends ConsumeLiquidBase<T>{
  public float minTemperature, maxTemperature;
  public float minFlammability, maxFlammability;
  public float minHeatCapacity, maxHeatCapacity;
  public float minViscosity, maxViscosity;
  public float minExplosiveness, maxExplosiveness;
  public int consGas = -1;
  public boolean isCoolant;

  public float usage;
  public Boolf<Liquid> filter;
  public Floatf<Liquid> usageMultiplier = e -> 1;
  public Floatf<Liquid> liquidEfficiency = l -> 1;

  public Liquid getCurrCons(T entity){
    for(LiquidStack liquid: consLiquids){
      if(entity.liquids.get(liquid.liquid) > 0.001f) return liquid.liquid;
    }
    return null;
  }

  public LiquidStack[] getCons(){
    if(consLiquids == null){
      Seq<LiquidStack> seq = new Seq<>();
      for(Liquid liquid: Vars.content.liquids()){
        if(filter != null && !filter.get(liquid)) continue;

        if(minTemperature != maxTemperature){
          if(liquid.temperature > maxTemperature || liquid.temperature < minTemperature) continue;
        }
        if(minFlammability != maxFlammability){
          if(liquid.flammability > maxFlammability || liquid.flammability < minFlammability) continue;
        }
        if(minHeatCapacity != maxHeatCapacity){
          if(liquid.heatCapacity > maxHeatCapacity || liquid.heatCapacity < minHeatCapacity) continue;
        }
        if(minExplosiveness != maxExplosiveness){
          if(liquid.explosiveness > maxExplosiveness || liquid.explosiveness < minExplosiveness) continue;
        }
        if(minViscosity != maxViscosity){
          if(liquid.viscosity > maxViscosity || liquid.viscosity < minViscosity) continue;
        }

        if(isCoolant && !liquid.coolant) continue;

        if((consGas == 1 && !liquid.gas) || (consGas == 0 && liquid.gas)) continue;

        seq.add(new LiquidStack(liquid, usage*usageMultiplier.get(liquid)));
      }

      consLiquids = seq.toArray(LiquidStack.class);
    }

    return consLiquids;
  }

  @Override
  public void buildIcons(Table table) {
    buildLiquidIcons(table, getCons(), true, displayLim);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void merge(BaseConsume<T> other){
    if(other instanceof ConsumeLiquidCond cons){
      minTemperature = Math.min(cons.minTemperature, minTemperature);
      minFlammability = Math.min(cons.minFlammability, minFlammability);
      minHeatCapacity = Math.min(cons.minHeatCapacity, minHeatCapacity);
      minViscosity = Math.min(cons.minViscosity, minViscosity);
      minExplosiveness = Math.min(cons.minExplosiveness, minExplosiveness);

      maxTemperature = Math.max(cons.maxTemperature, maxTemperature);
      maxFlammability = Math.max(cons.maxFlammability, maxFlammability);
      maxHeatCapacity = Math.max(cons.maxHeatCapacity, maxHeatCapacity);
      maxViscosity = Math.max(cons.maxViscosity, maxViscosity);
      maxExplosiveness = Math.max(cons.maxExplosiveness, maxExplosiveness);

      usage += cons.usage;
      Floatf<Liquid> mul = usageMultiplier, mulO = cons.usageMultiplier;
      usageMultiplier = l -> mul.get(l)*mulO.get(l);

      consLiquids = null;
      getCons();
    }
    else throw new IllegalArgumentException("only merge consume with same type");
  }

  @Override
  public void consume(T entity){}

  @Override
  public void update(T entity){
    LiquidStack[] cons = getCons();
    if(cons.length == 0) return;

    Liquid curr = getCurrCons(entity);
    if(curr == null) return;

    for(LiquidStack con: cons){
      if(con.liquid == curr){
        entity.liquids.remove(con.liquid, con.amount*parent.delta(entity)*multiple(entity));
        return;
      }
    }
  }

  @Override
  public void display(Stats stats){
    stats.add(Stat.input, table -> {
      table.row();
      table.table(t -> {
        t.defaults().left().fill().padLeft(6);
        t.add(Core.bundle.get("misc.liquid") + ":");

        int count = 0;
        for(LiquidStack stack: getCons()){
          if(count != 0) t.add("[gray]/[]");
          if(count != 0 && count % 6 == 0) t.row();
          t.add(new LiquidDisplay(stack.liquid, stack.amount*60, true));
          count++;
        }
      }).left().padLeft(5);
    });
  }

  @Override
  public void build(T entity, Table table){
    Seq<Liquid> list = content.liquids().select(l -> !l.isHidden() && filter.get(l));
    MultiReqImage image = new MultiReqImage();
    list.each(liquid -> image.add(new ReqImage(liquid.uiIcon, () ->
        entity.liquids != null && entity.liquids.get(liquid) > 0)));

    table.add(image).size(8 * 4);
  }

  @Override
  public float efficiency(T entity){
    LiquidStack[] cons = getCons();
    if(cons.length == 0) return 1;

    Liquid curr = getCurrCons(entity);
    if(curr == null) return 0;

    for(LiquidStack stack: cons){
      if(curr == stack.liquid){
        return liquidEfficiency.get(stack.liquid)*Mathf.clamp(entity.liquids.get(stack.liquid)/stack.amount);
      }
    }
    return 0;
  }

  @Override
  public Seq<Content> filter(){
    return Seq.with(getCons()).map(s -> s.liquid);
  }
}
