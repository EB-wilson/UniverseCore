package universeCore.world.producers;

import arc.Core;
import arc.math.Mathf;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.ItemDisplay;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ProducerBuildComp;

public class ProduceItems extends BaseProduce{
  /*控制是否随机产出产物(也就是是否为分离机)*/
  public boolean random = false;
  public ItemStack[] items;

  public ProduceItems(ItemStack[] items){
    this.items = items;
  }

  public void random(){
    this.random = true;
  }
  
  @Override
  public ProduceType<ProduceItems, Building> type() {
    return ProduceType.item;
  }
  
  @Override
  public void produce(ProducerBuildComp entity){
    if(!random){
      for(ItemStack stack: items){
        entity.getBuilding().items.add(stack.item, stack.amount);
      }
    }
    /*随机产出一种产物，amount参数变更为权*/
    else{
      int sum = 0;
      for(ItemStack stack : items) sum += stack.amount;

      int i = Mathf.random(sum);
      int count = 0;
      Item item = null;
      
      for(ItemStack stack : items){
        if(i >= count && i < count + stack.amount){
          item = stack.item;
          break;
        }
        count += stack.amount;
      }
      if(item != null) entity.getBuilding().items.add(item, 1);
    }
  }

  @Override
  public void update(ProducerBuildComp entity) {
  
  }
  
  @Override
  public void dump(ProducerBuildComp entity) {
    for(ItemStack stack : items){
      entity.getBuilding().dump(stack.item);
    }
  }
  
  @Override
  public void display(Stats stats) {
    stats.add(Stat.output, table -> {
      table.defaults().left();
      if(!random){
        table.row();
        table.add(Core.bundle.get("misc.item") + ":").left();
        for(ItemStack stack: items){
          table.add(new ItemDisplay(stack.item, stack.amount, true)).padRight(5);
        }
      }
      else{
        int total = 0;
        int n = items.length;
        table.row();
        table.add(Core.bundle.get("misc.item") + ":").left();
        table.row();
        for(ItemStack stack: items){
          table.add(new ItemDisplay(stack.item, 1, true));
          total += stack.amount;
          if(--n > 0) table.add("/");
        }
        table.row();
        for(ItemStack stack: items){
          table.add("[gray]" + (int)(((float)stack.amount)/((float)total)*100) + "%");
          table.add("|");
        }
      }
    });
  }
  
  @Override
  public boolean valid(ProducerBuildComp entity){
    for(ItemStack stack : items){
      if(entity.getBuilding().items.get(stack.item) > entity.getBlock().itemCapacity - stack.amount) return false;
    }
    return true;
  }
}
