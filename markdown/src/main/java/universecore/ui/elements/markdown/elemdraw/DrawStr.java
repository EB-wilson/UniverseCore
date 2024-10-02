package universecore.ui.elements.markdown.elemdraw;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.scene.style.Drawable;
import arc.util.Align;
import arc.util.pooling.Pools;
import universecore.ui.elements.markdown.Markdown;

public class DrawStr extends Markdown.DrawObj {
  String text;
  Font font;
  float scl;
  Color color;
  Drawable drawable;

  //use get
  DrawStr(){}

  public static DrawStr get(Markdown owner, String str, Font font, Color color, float ox, float oy, float scl, Drawable background) {
    DrawStr res = Pools.obtain(DrawStr.class, DrawStr::new);
    res.parent = owner;
    res.text = str;
    res.font = font;
    res.offsetX = ox;
    res.offsetY = oy;
    res.scl = scl;
    res.color = color;
    res.drawable = background;

    return res;
  }

  @Override
  protected void draw() {
    //调试用文本锚点
    //Fill.square(parent.x + offsetX, parent.y + parent.getHeight() + offsetY, 4, 45);
    if (drawable != null) {
      tmp2.set(Draw.getColor());
      GlyphLayout layout = GlyphLayout.obtain();
      layout.setText(font, text, tmp1.set(color).mul(Draw.getColor()), 0, Align.topLeft, false);

      drawable.draw(parent.x + offsetX - drawable.getLeftWidth(), parent.y + parent.getHeight() + offsetY - font.getLineHeight() - 2, layout.width + drawable.getLeftWidth() + drawable.getRightWidth(), font.getLineHeight() + 5);
      layout.free();

      Draw.color(tmp2);
      font.draw(text, parent.x + offsetX, parent.y + parent.getHeight() + offsetY, tmp1.set(color).mul(Draw.getColor()), scl, true, Align.topLeft);
    }
    else {
      font.draw(text, parent.x + offsetX, parent.y + parent.getHeight() + offsetY, tmp1.set(color).mul(Draw.getColor()), scl, true, Align.topLeft);
    }
  }

  @Override
  public void reset() {
    super.reset();
    text = null;
    font = null;
    scl = 0;
    color = null;
    drawable = null;
  }
}
