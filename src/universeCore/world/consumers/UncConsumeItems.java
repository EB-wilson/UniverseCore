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

public class UncConsumeItems extends BaseConsume{
  public ItemStack[] items;

  public UncConsumeItems(ItemStack[] items){
    this.items = items;
  }
  
  public UncConsumeType<UncConsumeItems, Building> type(){
    return UncConsumeType.item;
  }
  
  @Override
  public void consume(ConsumerBuildComp object){
    if(object instanceof Building){
      for(ItemStack stack : items){
        ((Building)object).items.remove(stack.item, stack.amount);
      }
    }
  }

  @Override
  public void update(ConsumerBuildComp entity) {

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
  public void build(ConsumerBuildComp entity, Table table) {
    for(ItemStack stack : items){
      table.add(new ReqImage(new ItemImage(stack.item.uiIcon, stack.amount),
      () -> entity.getBuilding().items != null && entity.getBuilding().items.has(stack.item, stack.amount))).padRight(8);
    }
    table.row();
  }

  @Override
  public boolean valid(ConsumerBuildComp entity){
    for(ItemStack stack: items){
      if(entity.getBuilding().items == null || entity.getBuilding().items.get(stack.item) < stack.amount) return false;
    }
    return true;
  }

  @Override
  public Item[] filter(ConsumerBuildComp entity){
    int i = 0;
    Item[] acceptItems = new Item[items.length];
    for(ItemStack stack: items){
      acceptItems[i++] = stack.item;
    }
    return acceptItems;
  }
}
