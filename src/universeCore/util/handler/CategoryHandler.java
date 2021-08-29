package universeCore.util.handler;

import arc.Core;
import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.type.Category;
import mindustry.ui.Styles;

import java.util.ArrayList;

import static mindustry.Vars.ui;

public class CategoryHandler{
  protected ArrayList<UncCategory> newCats = new ArrayList<>();
  protected boolean hasNew = false;
  
  public void handleBlockFrag(){
    if(!hasNew) return;
    Table togglerTable = FieldHandler.getValue(ui.hudfrag.blockfrag, "toggler");
    assert togglerTable != null;
  
    Table frame = (Table)togglerTable.getChildren().get(0);
    Table blockSelect = (Table)frame.getChildren().get(2);
    Table categories = (Table)frame.getChildren().get(3);
  
    Cell<?> pane = blockSelect.getCells().get(0);
    pane.height(240f);
  
    Seq<Element> catButtons = new Seq<>(categories.getChildren());
    catButtons.remove(0);
    
    for(UncCategory cat: newCats){
      ImageButton button = ((ImageButton)catButtons.find(e -> ("category-" + cat.cat.name()).equals(e.name)));
      if(button == null) continue;
      Drawable icon = new TextureRegionDrawable(Core.atlas.find(cat.icon));
      button.getStyle().imageUp = icon;
      button.resizeImage(icon.imageSize());
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
    }).size(125, 300);
  }
  
  public Category add(String name, int ordinal, String iconName){
    hasNew = true;
    UncCategory category = new UncCategory(name, ordinal, iconName);
    newCats.add(category);
    return category.cat;
  }
  
  public Category add(String name,  String iconName){
    return add(name, Category.values().length, iconName);
  }
  
  protected static class UncCategory{
    private static final EnumHandler<Category> handler = new EnumHandler<>(Category.class);
    
    Category cat;
    int ordinal;
    String icon;
    
    UncCategory(Category cat, String icon){
      this.cat = cat;
      this.icon = icon;
      ordinal = cat.ordinal();
    }
    
    UncCategory(String name, int ordinal, String icon){
      this(handler.addEnumItem(name, ordinal), icon);
      FieldHandler.setValue(Category.class, "all", null, Category.values());
      this.ordinal = ordinal;
    }
  }
}
