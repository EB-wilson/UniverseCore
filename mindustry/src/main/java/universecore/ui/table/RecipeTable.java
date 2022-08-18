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

/**生产消耗的统计信息显示器，用于显示几组生产列表的信息
 * @author EBwilson
 * @since 1.0*/
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
    for(StatCat cat : stat.toMap().keys()){
      OrderedMap<Stat, Seq<StatValue>> map = stat.toMap().get(cat);
      if(map.size == 0) continue;

      if(stat.useCategories){
        table.add("@category." + cat.name).color(Pal.accent).fillX();
        table.row();
      }

      for(Stat state : map.keys()){
        table.table(inset -> {
          inset.left();
          inset.add("[lightgray]" + state.localized() + ":[] ").left();
          Seq<StatValue> arr = map.get(state);
          for(StatValue value : arr){
            value.display(inset);
            inset.add().size(10f);
          }
        }).fillX().padLeft(10);
        table.row();
      }
    }
  }
}
