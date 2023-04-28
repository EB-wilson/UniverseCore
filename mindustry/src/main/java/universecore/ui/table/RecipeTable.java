package universecore.ui.table;

import arc.scene.ui.layout.Table;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;
import mindustry.world.meta.StatValue;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.FactoryBlockComp;

/**生产消耗的统计信息显示器，用于显示几组生产列表的信息
 * @author EBwilson
 * @since 1.0
 * */
public class RecipeTable extends Table{
  public Stats[] stats;
  
  /**构造一个对象后进行stats信息编辑，完成后调用build()方法才能完成列表构建*/
  public RecipeTable(int length){
    stats = new Stats[length];
  }
  
  public void build(){
    clearChildren();
    for(Stats stat : stats){
      table(Tex.pane, table -> {
        table.defaults().grow().left();
        buildRecipe(table, stat);
      }).grow().padTop(4).left();
      row();
    }
  }
  
  public void buildRecipe(Table table, Stats stat){
    FactoryBlockComp.buildStatTable(table, stat);
  }
}
