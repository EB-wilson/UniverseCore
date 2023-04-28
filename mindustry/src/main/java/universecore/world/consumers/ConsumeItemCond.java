package universecore.world.consumers;

import arc.Core;
import arc.func.Boolf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.ItemDisplay;
import mindustry.ui.MultiReqImage;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;

import static mindustry.Vars.content;

public class ConsumeItemCond<T extends Building & ConsumerBuildComp> extends ConsumeItemBase<T>{
  public float minRadioactivity, maxRadioactivity;
  public float minFlammability, maxFlammability;
  public float minCharge, maxCharge;
  public float minExplosiveness, maxExplosiveness;

  public int usage;
  public Boolf<Item> filter;

  public Item getCurrCons(T entity){
    for(ItemStack stack: consItems){
      if(entity.items.get(stack.item) >= stack.amount) return stack.item;
    }
    return null;
  }

  public ItemStack[] getCons(){
    if(consItems == null){
      Seq<ItemStack> seq = new Seq<>();
      for(Item item: Vars.content.items()){
        if(filter != null && !filter.get(item)) continue;

        if(minRadioactivity != maxRadioactivity){
          if(item.radioactivity > maxRadioactivity || item.radioactivity < minRadioactivity) continue;
        }
        if(minFlammability != maxFlammability){
          if(item.flammability > maxFlammability || item.flammability < minFlammability) continue;
        }
        if(minCharge != maxCharge){
          if(item.charge > maxCharge || item.charge < minCharge) continue;
        }
        if(minExplosiveness != maxExplosiveness){
          if(item.explosiveness > maxExplosiveness || item.explosiveness < minExplosiveness) continue;
        }

        seq.add(new ItemStack(item, usage));
      }
      consItems = seq.toArray(ItemStack.class);
    }

    return consItems;
  }

  @Override
  public void buildIcons(Table table) {
    buildItemIcons(table, getCons(), true, displayLim);
  }

  @SuppressWarnings({"rawtypes"})
  @Override
  public void merge(BaseConsume<T> other){
    if(other instanceof ConsumeItemCond cons){
      minRadioactivity = Math.min(cons.minRadioactivity, minRadioactivity);
      minFlammability = Math.min(cons.minFlammability, minFlammability);
      minCharge = Math.min(cons.minCharge, minCharge);
      minExplosiveness = Math.min(cons.minExplosiveness, minExplosiveness);

      maxRadioactivity = Math.max(cons.maxRadioactivity, maxRadioactivity);
      maxFlammability = Math.max(cons.maxFlammability, maxFlammability);
      maxCharge = Math.max(cons.maxCharge, maxCharge);
      maxExplosiveness = Math.max(cons.maxExplosiveness, maxExplosiveness);

      usage += cons.usage;

      consItems = null;
      getCons();
    }
    else throw new IllegalArgumentException("only merge consume with same type");
  }

  @Override
  public void consume(T entity){
    ItemStack[] cons = getCons();
    if(cons.length == 0) return;

    Item curr = getCurrCons(entity);
    if(curr == null) return;
    for(ItemStack con: cons){
      if(con.item == curr){
        entity.items.remove(con.item, con.amount);
      }
    }
  }

  @Override
  public void update(T entity){}

  @Override
  public void display(Stats stats){
    stats.add(Stat.input, table -> {
      table.row();
      table.table(t -> {
        t.defaults().left().fill().padLeft(6);
        t.add(Core.bundle.get("misc.item") + ":");

        int count = 0;
        for(ItemStack stack: getCons()){
          if(count != 0) t.add("[gray]/[]");
          if(count != 0 && count % 6 == 0) t.row();
          t.add(new ItemDisplay(stack.item, stack.amount*60, true));
          count++;
        }
      }).left().padLeft(5);
    });
  }

  @Override
  public void build(T entity, Table table){
    Seq<Item> list = content.items().select(l -> !l.isHidden() && filter.get(l));
    MultiReqImage image = new MultiReqImage();
    list.each(item -> image.add(new ReqImage(item.uiIcon, () ->
        entity.items != null && entity.items.get(item) > 0)));

    table.add(image).size(8 * 4);
  }

  @Override
  public float efficiency(T entity){
    ItemStack[] cons = getCons();
    if(cons.length == 0) return 1;

    Item curr = getCurrCons(entity);
    if(curr == null) return 0;

    for(ItemStack con: cons){
      if(curr == con.item) return 1;
    }
    return 0;
  }

  @Override
  public Seq<Content> filter(){
    return Seq.with(getCons()).map(s -> s.item);
  }
}
