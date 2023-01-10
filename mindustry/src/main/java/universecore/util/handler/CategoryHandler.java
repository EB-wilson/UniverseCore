package universecore.util.handler;

import arc.Core;
import arc.KeyBinds;
import arc.scene.Element;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.input.Binding;
import mindustry.type.Category;
import mindustry.ui.Styles;

import java.util.Arrays;

/**用于增加右下角方块选择栏分类条目的工具
 *
 * @since 1.0
 * @author EBwilson*/
public class CategoryHandler{
  protected final ObjectMap<Category, UncCategory> newCats = new ObjectMap<>();
  protected boolean hasNew = false;

  protected static final Binding empBind;

  static {
    EnumHandler<Binding> handler = new EnumHandler<>(Binding.class);

    empBind = handler.addEnumItemTail("unBind", (KeyBinds.KeybindValue) null);
  }
  
  public void handleBlockFrag(){
    if(!hasNew) return;
    Table catTable = FieldHandler.getValueDefault(Vars.ui.hudfrag.blockfrag, "blockCatTable");

    // frame.update(() -> {});
    Table blockSelect = (Table)catTable.getChildren().get(0);
    Table categories = (Table)catTable.getChildren().get(1);
  
    Cell<?> pane = blockSelect.getCells().get(0);
    pane.height(240f);
  
    Seq<Element> catButtons = new Seq<>(categories.getChildren());
    catButtons.remove(0);
    
    for(UncCategory cat: newCats.values()){
      ImageButton button = ((ImageButton)catButtons.find(e -> ("category-" + cat.cat.name()).equals(e.name)));
      if(button == null) continue;
      button.getStyle().imageUp = new TextureRegionDrawable(Core.atlas.find(cat.icon));
      button.resizeImage(32);
    }
    
    categories.clearChildren();
    categories.pane(t -> {
      t.defaults().size(50);
      int count = 0;
      for(Element element: catButtons){
        if(count++ % 2 == 0 && count != 0) t.row();
        t.add(element);
      }
      
      if(catButtons.size%2 != 0) t.image(Styles.black6);
    }).size(catButtons.size > 12? 125: 100, 300);
  }

  /**新增一个建筑类型到列表中，这会在游戏中的方块选择栏呈现
   *
   * @param name 类别的内部名称
   * @param ordinal 这个类别在选择栏的显示位置序数
   * @param iconName 这个类别的图标的资源文件名称*/
  public Category add(String name, int ordinal, String iconName){
    return add(name, ordinal, null, iconName);
  }

  /**新增一个建筑类型到列表中，这会在游戏中的方块选择栏呈现
   *
   * @param name 类别的内部名称
   * @param iconName 这个类别的图标的资源文件名称*/
  public Category add(String name, String iconName){
    return add(name, null, iconName);
  }

  /**新增一个建筑类型到列表中，这会在游戏中的方块选择栏呈现
   *
   * @param name 类别的内部名称
   * @param bind 这个类别绑定到的目标键位
   * @param iconName 这个类别的图标的资源文件名称*/
  public Category add(String name, Binding bind, String iconName){
    return add(name, Category.values().length, bind, iconName);
  }

  /**新增一个建筑类型到列表中，这会在游戏中的方块选择栏呈现
   *
   * @param name 类别的内部名称
   * @param ordinal 这个类别在选择栏的显示位置序数
   * @param bind 这个类别绑定到的目标键位
   * @param iconName 这个类别的图标的资源文件名称*/
  public Category add(String name, int ordinal, Binding bind, String iconName){
    hasNew = true;
    UncCategory category = new UncCategory(name, ordinal, bind, iconName);
    newCats.put(category.cat, category);

    return category.cat;
  }

  public void init(){
    Binding[] arr = FieldHandler.getValueDefault(Vars.ui.hudfrag.blockfrag, "blockSelect");
    if (arr.length < Category.all.length){
      arr = Arrays.copyOf(arr, Category.all.length);
      for (int i = 0; i < arr.length; i++) {
        UncCategory cat = newCats.get(Category.all[i]);
        if (arr[i] == null){
          arr[i] = cat != null? cat.bind: empBind;
        }
      }
    }

    FieldHandler.setValueDefault(Vars.ui.hudfrag.blockfrag, "blockSelect", arr);
  }
  
  protected static class UncCategory{
    private static final EnumHandler<Category> handler = new EnumHandler<>(Category.class);
    
    final Category cat;
    @Nullable
    final Binding bind;
    int ordinal;
    final String icon;
    
    UncCategory(Category cat, Binding bind, String icon){
      this.cat = cat;
      this.icon = icon;
      ordinal = cat.ordinal();
      this.bind = bind;
    }
    
    UncCategory(String name, int ordinal, Binding bind, String icon){
      this(handler.addEnumItem(name, ordinal), bind, icon);
      FieldHandler.setValueDefault(Category.class, "all", Category.values());
      this.ordinal = ordinal;
    }
  }
}
