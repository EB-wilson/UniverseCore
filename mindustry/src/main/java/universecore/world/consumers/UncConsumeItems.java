package universecore.world.consumers;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.ItemDisplay;
import mindustry.ui.ItemImage;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;

public class UncConsumeItems<T extends Building & ConsumerBuildComp> extends BaseConsume<T>{
  private static final ObjectMap<Item, ItemStack> TMP = new ObjectMap<>();

  public ItemStack[] items;

  public UncConsumeItems(ItemStack[] items){
    this.items = items;
  }
  
  public UncConsumeType<UncConsumeItems<?>> type(){
    return UncConsumeType.item;
  }
  
  @Override
  public TextureRegion icon(){
    return items[0].item.uiIcon;
  }

  @Override
  public void merge(BaseConsume<T> other){
    if(other instanceof UncConsumeItems cons){
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
    throw new IllegalArgumentException("only merge consume with same type");
  }

  @Override
  public void consume(T object){
    float f = multiple(object);
    for(ItemStack stack : items){
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
        for(ItemStack stack: items){
          t.add(new ItemDisplay(stack.item, stack.amount, parent.craftTime, true));
        }
      }).left().padLeft(5);
    });
  }

  @Override
  public void build(T entity, Table table) {
    for(ItemStack stack : items){
      int amount = (int)(stack.amount*multiple(entity));
      if(amount == 0 && !entity.consumer().valid()) amount = stack.amount;

      int n = amount;
      table.add(new ReqImage(new ItemImage(stack.item.uiIcon, stack.amount),
      () -> entity.items != null && entity.items.has(stack.item, n))).padRight(8);
    }
    table.row();
  }

  @Override
  public float efficiency(T entity){
    if(entity.items == null) return 0;
    for(ItemStack stack: items){
      if(entity.items == null || entity.items.get(stack.item) < stack.amount*multiple(entity)) return 0;
    }
    return 1;
  }

  @Override
  public Bits filter(T entity){
    Bits result = new Bits(Vars.content.items().size);
    for(ItemStack stack: items){
      result.set(stack.item.id);
    }
    return result;
  }
}
