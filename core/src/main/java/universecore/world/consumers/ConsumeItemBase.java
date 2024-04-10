package universecore.world.consumers;

import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.type.ItemStack;
import mindustry.ui.ItemDisplay;
import mindustry.ui.ItemImage;
import mindustry.ui.LiquidDisplay;
import universecore.components.blockcomp.ConsumerBuildComp;

public abstract class ConsumeItemBase<T extends ConsumerBuildComp> extends BaseConsume<T>{
  public ItemStack[] consItems;
  public int displayLim = 4;

  @Override
  public ConsumeType<?> type(){
    return ConsumeType.item;
  }

  public static void buildItemIcons(Table table, ItemStack[] items, boolean or, int limit) {
    int count = 0;
    for (ItemStack stack : items) {
      count++;
      if (count > 0 && or) table.add("/").set(Cell.defaults()).fill();
      if (limit >= 0 && count > limit){
        table.add("...");
        break;
      }

      table.add(new ItemImage(stack));
    }
  }
}
