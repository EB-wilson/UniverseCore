package universecore.ui.elements.markdown;

import arc.graphics.Color;
import arc.graphics.g2d.Font;

public class TextMirror {
  final String text;
  final Font font;
  final Color fontColor;
  final float offx, offy;
  final float width, height;

  TextMirror sub;

  TextMirror(String text, Font font, Color fontColor, float offx, float offy, float width, float height) {
    this.text = text;
    this.font = font;
    this.fontColor = fontColor;
    this.offx = offx;
    this.offy = offy;
    this.width = width;
    this.height = height;
  }
}
