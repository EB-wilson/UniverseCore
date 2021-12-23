package universeCore.world.consumers;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Bits;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.ItemDisplay;
import mindustry.ui.ItemImage;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universeCore.entityComps.blockComps.ConsumerBuildComp;

public class UncConsumeItems<T extends Building & ConsumerBuildComp> extends BaseConsume<T>{
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
  public void consume(T object){
    float f = object.consumeMultiplier(this);
    for(ItemStack stack : items){
      int amount = stack.amount*((int)Math.floor(f)) + Mathf.num(Math.random()<f%1);
      object.items.remove(stack.item, amount);
    }
  }

  @Override
  public void update(T entity) {

  }
  
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
      table.add(new ReqImage(new ItemImage(stack.item.uiIcon, stack.amount),
      () -> entity.items != null && entity.items.has(stack.item, stack.amount))).padRight(8);
    }
    table.row();
  }

  @Override
  public boolean valid(T entity){
    for(ItemStack stack: items){
      if(entity.items == null || entity.items.get(stack.item) < stack.amount*entity.consumeMultiplier(this)) return false;
    }
    return true;
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
