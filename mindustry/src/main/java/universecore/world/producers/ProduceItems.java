package universecore.world.producers;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.ItemDisplay;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ProducerBuildComp;

public class ProduceItems<T extends Building & ProducerBuildComp> extends BaseProduce<T>{
  private static final ObjectMap<Item, ItemStack> TMP = new ObjectMap<>();

  public boolean showPerSecond = true;

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
  public Color color(){
    return items[0].item.color;
  }

  @Override
  public TextureRegion icon(){
    return items[0].item.uiIcon;
  }

  @Override
  public void merge(BaseProduce<T> other){
    if(other instanceof ProduceItems cons){
      TMP.clear();
      for(ItemStack stack: items){
        TMP.put(stack.item, stack);
      }

      for(ItemStack stack: cons.items){
        TMP.get(stack.item, () -> new ItemStack(stack.item, 0)).amount += stack.amount;
      }

      items = TMP.values().toSeq().sort((a, b) -> a.item.id - b.item.id).toArray(ItemStack.class);
      return;
    }
    throw new IllegalArgumentException("only merge production with same type");
  }

  @Override
  public void produce(T entity){
    float f = multiple(entity);
    if(!random){
      for(ItemStack stack: items){
        int amount = stack.amount*((int)Math.floor(f)) + Mathf.num(Math.random()<f%1);
        amount = Math.min(amount, entity.block.itemCapacity - entity.items.get(stack.item));
        for (int i = 0; i < amount; i++) {
          entity.handleItem(entity, stack.item);
        }
      }
    }
    /*随机产出一种产物，amount参数变更为权*/
    else{
      int sum = 0;
      for(ItemStack stack : items){
        sum += stack.amount;
      }

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
        amount = Math.min(amount, entity.block.itemCapacity - entity.items.get(item));
        for (int l = 0; l < amount; l++) {
          entity.handleItem(entity, item);
        }
      }
    }
  }

  @Override
  public void update(Building entity) {
  
  }
  
  @Override
  public void dump(Building entity) {
    for(ItemStack stack : items){
      if(entity.items.get(stack.item) > 0) entity.dump(stack.item);
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
            t.add(showPerSecond? new ItemDisplay(stack.item, stack.amount, parent.cons.craftTime, true):
                new ItemDisplay(stack.item, stack.amount, true));
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
  public boolean valid(T entity){
    if(entity.items == null) return false;

    boolean res = false;
    for(ItemStack stack : items){
      if(entity.items.get(stack.item) + stack.amount*multiple(entity) > entity.block.itemCapacity){
        if(blockWhenFull) return false;
      }
      else res = true;
    }
    return res;
  }
}
