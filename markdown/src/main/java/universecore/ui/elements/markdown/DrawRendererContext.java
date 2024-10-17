package universecore.ui.elements.markdown;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.style.Drawable;
import arc.scene.ui.Tooltip;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ui.Fonts;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import universecore.ui.elements.markdown.elemdraw.DrawBoard;
import universecore.ui.elements.markdown.elemdraw.DrawClickable;
import universecore.ui.elements.markdown.elemdraw.DrawCode;
import universecore.ui.elements.markdown.elemdraw.DrawStr;

public abstract class DrawRendererContext {
  public final Markdown element;

  private final Seq<Markdown.DrawObj> drawObjs = new Seq<>();
  private final ObjectMap<Node, TextureRegion> imgCache = new ObjectMap<>();

  public int boardLayers;
  public int listLayer;

  public float padding;
  public float currScl;

  public float rendOff;
  public float lineOff;
  public float totalHeight;

  public TextMirror lastText;
  public Font currFont;
  public Color currFontColor;

  protected DrawRendererContext(Markdown element) {
    this.element = element;
  }

  public abstract void render(Node node);

  public Iterable<Markdown.DrawObj> renderResult() {
    return drawObjs;
  }

  public void draw(Markdown.DrawObj obj){
    drawObjs.add(obj);
  }

  public TextureRegion imageCache(Image image, Prov<TextureRegion> o) {
    return imgCache.get(image, o);
  }

  public void init(){
    boardLayers = 0;
    listLayer = 0;
    padding = 0;
    rendOff = 0;
    lineOff = 0;
    currScl = 1;
    totalHeight = 0;
    lastText = null;
    currFont = element.getStyle().font;
    currFontColor = element.getStyle().textColor;
    drawObjs.clear();
  }

  public void clearCache(){
    init();
    imgCache.clear();
  }

  public void updateTmpHeight(float h) {
    totalHeight = Math.max(totalHeight, h);
  }

  public TextMirror makeStr(String str, Font font, Color color) {
    return makeStr(str, font, null, null, color);
  }

  public TextMirror makeStr(String str, Font font, Color color, String openUrl) {
    return makeStr(str, font, openUrl, null, color);
  }

  public TextMirror makeStr(String str, Font font, Drawable background, Color color) {
    return makeStr(str, font, null, background, color);
  }

  public TextMirror makeStr(String str, Font font, String openUrl, Drawable background, Color color) {
    float maxWidth = element.getWidth() <= font.getSpaceXadvance()*3? Float.MAX_VALUE: element.getWidth() - rendOff;
    float tmp = 0;
    int index = 0;

    float width = 0;
    updateTmpHeight(font.getLineHeight()*currScl);
    for (int c : str.chars().toArray()) {
      Font.Glyph glyph = font.getData().getGlyph((char) c);
      if (glyph == null){
        index++;
        continue;
      }

      tmp += glyph.xadvance*currScl*font.getScaleX();
      if (tmp > maxWidth){
        break;
      }
      width = tmp;
      index++;
    }

    TextMirror res = lastText;
    if (width > 0){
      rendOff += background != null? background.getLeftWidth(): 0;
      if (res == null) res = new TextMirror(str.substring(0, index), font, color, rendOff, -lineOff, width, totalHeight);
      else res.sub = new TextMirror(str.substring(0, index), font, color, rendOff, -lineOff, width, totalHeight);

      draw(openUrl != null?
          DrawClickable.get(element, str.substring(0, index), font,
              () -> Core.app.openURI(openUrl), new Tooltip(t -> t.table(element.getStyle().board).get().add(openUrl)),
              color, rendOff, -lineOff, currScl
          ):
          DrawStr.get(element, str.substring(0, index), font, color, rendOff, -lineOff, currScl, background));
      rendOff += width + (background != null? background.getRightWidth(): 0) + font.getSpaceXadvance()*font.getScaleX();
    }
    else if (res == null) res = new TextMirror("", font, color, rendOff, -lineOff, 0, totalHeight);
    else res.sub = new TextMirror("", font, color, rendOff, -lineOff, 0, totalHeight);

    lastText = res;

    if (index < str.length()){
      row();
      res.sub = makeStr(str.substring(index), font, openUrl, background, color);
    }

    return res;
  }

  public void makeCodeBox(String lang, String code) {
    Markdown.MarkdownStyle style = element.getStyle();

    padding += 4;
    lineOff += style.linesPadding;
    row();
    float begin = lineOff;
    lineOff += style.linesPadding*2;
    DrawCode pane = DrawCode.get(element, lang, code, style.codeFont, rendOff, -lineOff, style.codeBlockStyle);
    updateTmpHeight(pane.height() + style.linesPadding);
    draw(pane);
    padding -= 4;
    row();
    lineOff += style.linesPadding*2;
    draw(DrawBoard.get(element, style.codeBlockBack, 0, lineOff - begin, -begin, -begin));

    DrawClickable c = DrawClickable.get(element, Core.bundle.get("misc.copy"), Fonts.outline, () -> {
          Core.app.setClipboardText(code);
          Vars.ui.showInfoFade(Core.bundle.get("infos.copyToClip"));
        },
        null, style.subTextColor, pane.width() - 64, -begin - style.linesPadding*2 - 8, 1
    );
    draw(c);
    Element e = c.getElem();
    e.color.a = 0.4f;
    e.hovered(() -> e.actions(Actions.alpha(1, 0.5f)));
    e.exited(() -> e.actions(Actions.alpha(0.4f, 0.5f)));
  }

  public void row() {
    Markdown.MarkdownStyle style = element.getStyle();
    element.prefWidth = Math.max(element.prefWidth, rendOff);

    rendOff = padding*style.font.getSpaceXadvance();
    lineOff += totalHeight + style.linesPadding*currScl;

    element.prefHeight = lineOff;
    totalHeight = 0;
  }
}