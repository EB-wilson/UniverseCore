package universecore.util.handler;

import arc.Core;
import arc.scene.Element;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.input.Binding;
import mindustry.type.Category;
import mindustry.ui.Styles;
import mindustry.ui.fragments.PlacementFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class CategoryHandler{
  protected final ArrayList<UncCategory> newCats = new ArrayList<>();
  protected boolean hasNew = false;
  protected final Field selects;
  
  public CategoryHandler(){
    try{
      selects = PlacementFragment.class.getDeclaredField("blockSelect");
      selects.setAccessible(true);
    }catch(NoSuchFieldException e){
      throw new RuntimeException(e);
    }
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
    
    for(UncCategory cat: newCats){
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
  
  public Category add(String name, int ordinal, String iconName){
    return add(name, ordinal, null, iconName);
  }
  
  public Category add(String name, int ordinal, Binding bind, String iconName){
    hasNew = true;
    UncCategory category = new UncCategory(name, ordinal, bind, iconName);
    newCats.add(category);
    //binds.add(ordinal, bind);
    //FieldHandler.setValue(selects, ui.hudfrag.blockfrag, binds.toArray(Binding[]::new));
    return category.cat;
  }
  
  public Category add(String name, String iconName){
    return add(name, null, iconName);
  }
  
  public Category add(String name, Binding bind, String iconName){
    return add(name, Category.values().length, bind, iconName);
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
