package universecore.world.consumers;

import arc.scene.ui.Image;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.core.UI;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import universecore.components.blockcomp.ConsumerBuildComp;

public abstract class ConsumeLiquidBase<T extends ConsumerBuildComp> extends BaseConsume<T>{
  public LiquidStack[] consLiquids;
  public int displayLim = 4;

  @Override
  public ConsumeType<?> type(){
    return ConsumeType.liquid;
  }

  public static void buildLiquidIcons(Table table, LiquidStack[] liquids, boolean or, int limit){
    int count = 0;
    for (LiquidStack stack: liquids) {
      count++;
      if (count > 0 && or) table.add("/").set(Cell.defaults()).fillX();
      if (limit >= 0 && count > limit){
        table.add("...");
        break;
      }

      table.stack(
          new Table(o -> {
            o.left();
            o.add(new Image(stack.liquid.fullIcon)).size(32f).scaling(Scaling.fit);
          }),
          new Table(t -> {
            t.left().bottom();
            t.add(stack.amount*60 >= 1000 ? UI.formatAmount((long) (stack.amount*60))+ "/s" : stack.amount*60 + "/s").style(Styles.outlineLabel);
            t.pack();
          })
      );
    }
  }
}
