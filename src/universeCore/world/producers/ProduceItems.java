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

public class ProduceItems<T extends Building & ProducerBuildComp> extends BaseProduce<T>{
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
  public ProduceType<ProduceItems<?>> type() {
    return ProduceType.item;
  }
  
  @Override
  public void produce(T entity){
    float f = entity.productMultiplier(this);
    if(!random){
      for(ItemStack stack: items){
        int amount = stack.amount*((int)Math.floor(f)) + Mathf.num(Math.random()<f%1);
        entity.items.add(stack.item, amount);
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
      if(item != null){
        int amount = (int)(Math.floor(f) + Mathf.num(Math.random()<f%1));
        entity.items.add(item, amount);
      }
    }
  }

  @Override
  public void update(Building entity) {
  
  }
  
  @Override
  public void dump(Building entity) {
    for(ItemStack stack : items){
      entity.dump(stack.item);
    }
  }
  
  @Override
  public void display(Stats stats) {
    stats.add(Stat.output, table -> {
      table.row();
      table.table(t -> {
        t.defaults().left().fill().padLeft(6);
        t.add(Core.bundle.get("misc.item") + ":").left();
        if(!random){
          for(ItemStack stack: items){
            t.add(new ItemDisplay(stack.item, stack.amount, parent.cons.craftTime, true));
          }
        }
        else{
          int[] total = {0};
          int[] n = {items.length, items.length};
          t.table(item -> {
            for(ItemStack stack: items){
              item.add(new ItemDisplay(stack.item, 0, true));
              total[0] += stack.amount;
              if(--n[0] > 0) item.add("/");
            }
            item.row();
            for(ItemStack stack: items){
              item.add("[gray]" + (int)(((float)stack.amount)/((float)total[0])*100) + "%");
              if(--n[1] > 0) item.add();
            }
          });
        }
      }).left().padLeft(5);
    });
  }
  
  @Override
  public boolean valid(Building entity){
    for(ItemStack stack : items){
      if(entity.items.get(stack.item) > entity.block.itemCapacity - stack.amount) return false;
    }
    return true;
  }
}
