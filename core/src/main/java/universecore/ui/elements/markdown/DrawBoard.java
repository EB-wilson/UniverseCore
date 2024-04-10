package universecore.ui.elements.markdown;

import arc.scene.style.Drawable;
import arc.util.pooling.Pools;

public class DrawBoard extends Markdown.DrawObj {
  float height;
  Drawable drawable;
  int lay;

  //use get
  DrawBoard(){}

  static DrawBoard get(Markdown owner, Drawable drawable, int layer, float height, float offX, float offY) {
    DrawBoard res = Pools.obtain(DrawBoard.class, DrawBoard::new);
    res.parent = owner;
    res.drawable = drawable;
    res.height = height;
    res.offsetX = offX;
    res.offsetY = offY;
    res.lay = layer;

    return res;
  }

  @Override
  public int priority() {
    return lay == 0? 0: -(1000 - lay);
  }

  @Override
  public void reset() {
    super.reset();
    parent = null;
    drawable = null;
    height = 0;
    lay = 0;
  }

  @Override
  void draw() {
    drawable.draw(parent.x + offsetX, parent.y + parent.getHeight() + offsetY - height, parent.getWidth() - offsetX, height);
  }
}
