package universecore.world.consumers;

import arc.Core;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.ctype.Content;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValues;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;

public class ConsumeItems<T extends Building & ConsumerBuildComp> extends ConsumeItemBase<T>{
  private static final ObjectMap<Item, ItemStack> TMP = new ObjectMap<>();

  public boolean showPerSecond = true;

  public ConsumeItems(ItemStack[] items){
    this.consItems = items;
  }

  @Override
  public void buildIcons(Table table) {
    buildItemIcons(table, consItems, false, displayLim);
  }

  @SuppressWarnings({"rawtypes", "DuplicatedCode"})
  @Override
  public void merge(BaseConsume<T> other){
    if(other instanceof ConsumeItems cons){
      TMP.clear();
      for(ItemStack stack: consItems){
        TMP.put(stack.item, stack);
      }

      for(ItemStack stack: cons.consItems){
        TMP.get(stack.item, () -> new ItemStack(stack.item, 0)).amount += stack.amount;
      }

      consItems = TMP.values().toSeq().sort((a, b) -> a.item.id - b.item.id).toArray(ItemStack.class);
      return;
    }
    throw new IllegalArgumentException("only merge consume with same type");
  }

  @Override
  public void consume(T object){
    float f = multiple(object);
    for(ItemStack stack : consItems){
      int amount = stack.amount*((int)Math.floor(f)) + Mathf.num(Math.random()<f%1);
      object.items.remove(stack.item, amount);
    }
  }

  @Override
  public void update(T entity) {}
  
  @Override
  public void display(Stats stats) {
    stats.add(Stat.input, table -> {
      table.row();
      table.table(t -> {
        t.defaults().left().grow().fill().padLeft(6);
        t.add(Core.bundle.get("misc.item") + ":");
        for(ItemStack stack: consItems){
          t.add(showPerSecond?
              StatValues.displayItem(stack.item, stack.amount, parent.craftTime, true):
              StatValues.displayItem(stack.item, stack.amount, true));
        }
      }).left().padLeft(5);
    });
  }

  @Override
  public void build(T entity, Table table) {
    for(ItemStack stack : consItems){
      int amount = (int)(stack.amount*multiple(entity));
      if(amount == 0 && !entity.consumer().valid()) amount = stack.amount;

      int n = amount;
      table.add(new ReqImage(StatValues.stack(stack),
      () -> entity.items != null && entity.items.has(stack.item, n))).padRight(8);
    }
    table.row();
  }

  @Override
  public float efficiency(T entity){
    if(entity.items == null) return 0;
    for(ItemStack stack: consItems){
      if(entity.items == null || entity.items.get(stack.item) < stack.amount*multiple(entity)) return 0;
    }
    return 1;
  }

  @Override
  public Seq<Content> filter(){
    return Seq.with(consItems).map(s -> s.item);
  }
}
