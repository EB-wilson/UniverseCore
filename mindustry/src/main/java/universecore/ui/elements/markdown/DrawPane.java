package universecore.ui.elements.markdown;

import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.util.pooling.Pools;

public class DrawPane extends Markdown.DrawObj implements Markdown.ActivityDrawer {
  ScrollPane pane;
  Label label;
  float maxHeight;

  //use get
  DrawPane(){}

  static DrawPane get(Markdown owner, String str, Font textFont, Color color, float ox, float oy, float maxHeight, ScrollPane.ScrollPaneStyle paneStyle) {
    DrawPane res = Pools.obtain(DrawPane.class, DrawPane::new);
    res.parent = owner;
    res.offsetX = ox;
    res.offsetY = oy;
    res.maxHeight = maxHeight;

    res.label = new Label(str, new Label.LabelStyle() {{
      font = textFont;
      fontColor = color;
    }});

    res.pane = new ScrollPane(res.label, paneStyle);

    return res;
  }

  @Override
  void draw() {
  }

  @Override
  public void reset() {
    super.reset();
    label = null;
    pane = null;
    maxHeight = 0;
  }

  @Override
  public Element getElem() {
    return pane;
  }

  @Override
  public float width() {
    return parent.getWidth() - offsetX - 12;
  }

  @Override
  public float height() {
    return Math.min(label.getHeight(), maxHeight);
  }
}
