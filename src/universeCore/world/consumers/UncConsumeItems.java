package universeCore.world.consumers;

import arc.Core;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.ItemDisplay;
import mindustry.ui.ItemImage;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ConsumerBuildComp;

public class UncConsumeItems extends BaseConsume<Building>{
  public ItemStack[] items;

  public UncConsumeItems(ItemStack[] items){
    this.items = items;
  }
  
  public UncConsumeType<UncConsumeItems> type(){
    return UncConsumeType.item;
  }
  
  @Override
  public void consume(Building object){
    for(ItemStack stack : items){
      object.items.remove(stack.item, stack.amount);
    }
  }

  @Override
  public void update(Building entity) {

  }
  
  @Override
  public void display(Stats stats) {
    stats.add(Stat.input, table -> {
      table.row();
      table.table(t -> {
        t.defaults().left().grow().fill().padLeft(6);
        t.add(Core.bundle.get("misc.item") + ":");
        for(ItemStack stack: items){
          t.add(new ItemDisplay(stack.item, stack.amount, true));
        }
      }).left().padLeft(5);
    });
  }

  @Override
  public void build(Building entity, Table table) {
    for(ItemStack stack : items){
      table.add(new ReqImage(new ItemImage(stack.item.uiIcon, stack.amount),
      () -> entity.items != null && entity.items.has(stack.item, stack.amount))).padRight(8);
    }
    table.row();
  }

  @Override
  public boolean valid(Building entity){
    for(ItemStack stack: items){
      if(entity.items == null || entity.items.get(stack.item) < stack.amount) return false;
    }
    return true;
  }

  @Override
  public Item[] filter(Building entity){
    int i = 0;
    Item[] acceptItems = new Item[items.length];
    for(ItemStack stack: items){
      acceptItems[i++] = stack.item;
    }
    return acceptItems;
  }
}
