package universecore.world.consumers;

import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.core.UI;
import mindustry.gen.Building;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import universecore.components.blockcomp.ConsumerBuildComp;

public abstract class ConsumeLiquidBase<T extends Building & ConsumerBuildComp> extends BaseConsume<T>{
  public LiquidStack[] consLiquids;
  public int displayLim = 4;

  @Override
  public ConsumeType<?> type(){
    return ConsumeType.liquid;
  }

  @Override
  public void buildBars(T entity, Table bars) {
    for (LiquidStack stack : consLiquids) {
      bars.add(new Bar(
          () -> stack.liquid.localizedName,
          () -> stack.liquid.barColor != null? stack.liquid.barColor: stack.liquid.color,
          () -> Math.min(entity.liquids.get(stack.liquid) / entity.block.liquidCapacity, 1f)
      ));
      bars.row();
    }
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
            t.add(stack.amount*60 >= 1000 ? UI.formatAmount((long) (stack.amount*60))+ "/s" : Mathf.round(stack.amount*600)/10f + "/s").style(Styles.outlineLabel);
            t.pack();
          })
      );
    }
  }
}
