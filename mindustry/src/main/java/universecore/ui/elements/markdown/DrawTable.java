package universecore.ui.elements.markdown;

import arc.scene.Element;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.pooling.Pools;
import mindustry.ui.Styles;

public class DrawTable extends Markdown.DrawObj implements Markdown.ActivityDrawer {
  Table table;
  ScrollPane pane;

  //use get
  DrawTable(){}

  static DrawTable get(Markdown owner, Table table, float ox, float oy) {
    DrawTable res = Pools.obtain(DrawTable.class, DrawTable::new);
    res.parent = owner;
    res.table = table;
    res.pane = new ScrollPane(table, Styles.noBarPane);
    res.offsetX = ox;
    res.offsetY = oy;

    return res;
  }

  @Override
  void draw() {
  }

  @Override
  public Element getElem() {
    return pane;
  }

  @Override
  public float width() {
    return Math.min(parent.getWidth(), table.getPrefWidth());
  }

  @Override
  public float height() {
    return table.getPrefHeight();
  }

  @Override
  public void reset() {
    super.reset();
    table = null;
    pane = null;
  }
}
