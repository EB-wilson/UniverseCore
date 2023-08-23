package universecore.ui.elements.markdown;

import arc.Core;
import arc.func.Floatp;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import arc.util.serialization.Base64Coder;
import mindustry.Vars;
import mindustry.ui.Fonts;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.*;
import org.commonmark.ext.ins.Ins;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import universecore.util.UrlDownloader;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Markdown extends Group {
  public static final String IMAGE_PNG_BASE_64 = "data:image/png;base64,";
  private final Node node;
  private final MarkdownStyle style;

  private final Seq<DrawObj> drawObjs = new Seq<>();
  private final ObjectMap<Node, TextureRegion> imgCache = new ObjectMap<>();

  float prefWidth, prefHeight;
  boolean prefInvalid = true;
  float lastPrefHeight;

  protected static Parser parser;

  static {
    List<Extension> extensions = Arrays.asList(
        TablesExtension.create(),
        InsExtension.create(),
        StrikethroughExtension.create(),
        CurtainExtension.create()
    );

    parser = Parser.builder()
        .extensions(extensions)
        .build();
  }

  public Markdown(String md, MarkdownStyle style){
    node = parser.parse(md);
    touchable = Touchable.childrenOnly;

    this.style = style;
  }

  /**internal usage*/
  private Markdown(Markdown parent, Node node, MarkdownStyle style){
    this.node = node;
    touchable = Touchable.childrenOnly;

    this.style = style;
  }

  @Override
  public void layout() {
    prefHeight = prefWidth = 0;

    for (DrawObj obj : drawObjs) {
      obj.free();
    }
    drawObjs.clear();
    
    float[] rendOff = {0}, lineOff = {0},
        tmpHeight = {0};
    node.accept(new AbsExtensionVisitor() {
      final AbsExtensionVisitor outer = this;

      TextMirror lastText;
      Font currFont = style.font;
      Color currFontColor = style.textColor;
      int padding;
      float currScl = 1;

      int listLayer = 0;
      int boardLayers = 0;

      @Override
      protected void visitChildren(Node parent) {
        super.visitChildren(parent);
      }

      @Override
      public void visit(BlockQuote blockQuote) {
        boardLayers++;
        padding += 4;
        float begRend = rendOff[0];
        lineOff[0] += style.linesPadding;
        row();
        float begin = lineOff[0];
        lineOff[0] += style.linesPadding*2;
        super.visit(blockQuote);
        padding -= 4;
        row();
        lineOff[0] += style.linesPadding;
        drawObjs.add(DrawBoard.get(Markdown.this, style.board, boardLayers, lineOff[0] - begin, begRend, -begin));
        lineOff[0] += style.linesPadding*2;
        boardLayers--;
      }

      @Override
      public void visit(BulletList bulletList) {
        row();
        padding += 4;
        listLayer++;
        bulletList.accept(new AbsExtensionVisitor() {
          @Override
          protected void visitChildren(Node parent) {
            Node node = parent.getFirstChild();

            while (node != null) {
              Node next = node.getNext();
              row();

              float ox = rendOff[0], oy = -lineOff[0] - currFont.getLineHeight();
              Drawable drawer = style.listMarks[(listLayer - 1)%style.listMarks.length];
              float scl = currScl;
              Font cu = currFont;
              drawObjs.add(new DrawObj() {
                @Override
                void draw() {
                  Draw.color(style.textColor);
                  Draw.alpha(color.a*parentAlpha);
                  drawer.draw(
                      x + ox + drawer.getLeftWidth()*scl, y + getHeight() + oy,
                      cu.getLineHeight()*scl, cu.getLineHeight()*scl
                  );
                }
              });
              rendOff[0] += (currFont.getLineHeight() + drawer.getLeftWidth() + drawer.getRightWidth())*currScl;
              updateTmpHeight((currFont.getLineHeight() + drawer.getBottomHeight() + drawer.getTopHeight())*currScl);

              node.accept(outer);
              row();
              node = next;
            }
          }
        });
        listLayer--;
        padding -= 4;
        row();
      }

      @Override
      public void visit(Code code) {
        lastText = makeStr(code.getLiteral(), style.codeFont, style.codeBack, style.subTextColor);
      }

      @Override
      public void visit(Emphasis emphasis) {
        if (emphasis.getClosingDelimiter().length() > 1) return;

        Font last = currFont;
        Color lastColor = currFontColor;
        currFont = style.emFont;
        currFontColor = style.emColor;
        //rendOff[0] -= currFont.getSpaceXadvance()/2; //只在这里有半个多余的空格长度，原因不明，手动回退
        super.visit(emphasis);
        currFont = last;
        currFontColor = lastColor;
      }

      @Override
      public void visit(StrongEmphasis strongEmphasis) {
        Font last = currFont;
        Color lastColor = currFontColor;
        currFont = style.strongFont;
        currFontColor = strongEmphasis.getClosingDelimiter().length()%2 == 0? lastColor: style.emColor;
        super.visit(strongEmphasis);
        currFont = last;
        currFontColor = lastColor;
      }

      @Override
      public void visit(FencedCodeBlock fencedCodeBlock) {
        lastText = null;
        makeCodeBox(fencedCodeBlock.getLiteral());
      }

      @Override
      public void visit(HardLineBreak hardLineBreak) {
        row();
      }

      @Override
      public void visit(Heading heading) {
        lastText = null;
        row();
        float re = currScl;
        currScl = 5f/heading.getLevel();
        lineOff[0] += currScl*style.font.getLineHeight()/2;
        super.visit(heading);
        lineOff[0] += currScl*style.font.getLineHeight()/2;
        currScl = re;
        row();
      }

      @Override
      public void visit(ThematicBreak thematicBreak) {
        lastText = null;
        row();
        drawObjs.add(DrawHr.get(Markdown.this, style.lineColor, -lineOff[0]));
        lineOff[0] += 4;
        row();
        super.visit(thematicBreak);
      }

      @Override
      public void visit(HtmlInline htmlInline) {
        throw new IllegalArgumentException("not support raw html");
      }

      @Override
      public void visit(HtmlBlock htmlBlock) {
        throw new IllegalArgumentException("not support raw html");
      }

      @Override
      public void visit(Image image) {
        row();

        TextureRegion region = Core.atlas.find(image.getDestination());
        if (image.getDestination().startsWith(IMAGE_PNG_BASE_64)){
          region = imgCache.get(image, () -> new TextureRegion(new Texture(new Pixmap(Base64Coder.decode(image.getDestination().replace(IMAGE_PNG_BASE_64, ""))))));
        }
        else if (!Core.atlas.isFound(region)){
          region = imgCache.get(image, () -> UrlDownloader.downloadImg(image.getDestination(), Core.atlas.find("nomap")));
        }

        float w = Math.min(width, region.width), h = region.height*(w/region.width);
        if (image.getTitle() != null) h += 4 + style.subFont.getLineHeight();
        drawObjs.add(DrawImg.get(Markdown.this, region, image.getTitle(), -lineOff[0], style.subFont, style.subTextColor));
        lineOff[0] += h;
        row();
      }

      @Override
      public void visit(IndentedCodeBlock indentedCodeBlock) {
        lastText = null;
        makeCodeBox(indentedCodeBlock.getLiteral());
      }

      @Override
      public void visit(Link link) {
        Color old = currFontColor;
        currFontColor = style.linkColor;
        link.accept(new AbsExtensionVisitor() {
          @Override
          public void visit(Text text) {
            makeStr(text.getLiteral().trim(), currFont, currFontColor, link.getDestination());
          }
          @Override
          public void visit(Emphasis emphasis) {
            if (emphasis.getClosingDelimiter().length() > 1) return;

            Font last = currFont;
            Color lastColor = currFontColor;
            currFont = style.emFont;
            currFontColor = style.emColor;
            super.visit(emphasis);
            currFont = last;
            currFontColor = lastColor;
          }
          @Override
          public void visit(StrongEmphasis strongEmphasis) {
            Font last = currFont;
            Color lastColor = currFontColor;
            currFont = style.strongFont;
            currFontColor = strongEmphasis.getClosingDelimiter().length()%2 == 0? lastColor: style.emColor;
            super.visit(strongEmphasis);
            currFont = last;
            currFontColor = lastColor;
          }
        });
        currFontColor = old;
      }

      @Override
      public void visit(OrderedList orderedList) {
        row();
        padding += 4;
        orderedList.accept(new AbsExtensionVisitor() {
          @Override
          protected void visitChildren(Node parent) {
            Node node = parent.getFirstChild();

            int begin = orderedList.getStartNumber();
            while (node != null) {
              Node next = node.getNext();
              row();
              lastText = makeStr(Integer.toString(begin++) + orderedList.getDelimiter() + " ", currFont, currFontColor);
              node.accept(outer);
              row();
              node = next;
            }
          }
        });
        padding -= 4;
        row();
      }

      @Override
      public void visit(SoftLineBreak softLineBreak) {
        row();
      }

      @Override
      public void visit(Text text) {
        lastText = makeStr(text.getLiteral().trim(), currFont, currFontColor);
      }

      @Override
      public void visit(Ins ins) {
        lastText = null;
        super.visit(ins);
        TextMirror orig = lastText;
        while (orig != null){
          drawObjs.add(DrawLine.get(Markdown.this, lastText.fontColor, lastText.offx, lastText.offy - lastText.height, lastText.width));

          orig = orig.sub;
        }
      }

      @Override
      public void visit(Strikethrough strikethrough) {
        lastText = null;
        super.visit(strikethrough);
        TextMirror orig = lastText;
        while (orig != null){
          drawObjs.add(DrawLine.get(Markdown.this, lastText.fontColor, lastText.offx, lastText.offy - lastText.height/2, lastText.width));

          orig = orig.sub;
        }
      }

      @Override
      public void visit(LinkReferenceDefinition linkReferenceDefinition) {
        throw new UnsupportedOperationException("unsupport yet");
      }

      @Override
      public void visit(Paragraph paragraph) {
        lastText = null;
        super.visit(paragraph);
        row();
      }

      @Override
      public void visit(Curtain curtain) {
        lastText = null;
        float begin = rendOff[0];
        rendOff[0] += style.curtain.getLeftWidth();
        super.visit(curtain);
        rendOff[0] += style.curtain.getRightWidth();
        drawObjs.add(DrawCurtain.get(Markdown.this, style.curtain, begin, lastText.offy,
            lastText.width + style.curtain.getLeftWidth() + style.curtain.getRightWidth(),
            lastText.height + style.curtain.getTopHeight() + style.curtain.getBottomHeight()
        ));
      }

      @Override
      public void visit(TableBlock table) {
        Table layoutTable = new Table();

        table.accept(new AbsExtensionVisitor() {
          boolean r;
          int currCol;
          Table[] columns;

          @Override
          public void visit(TableHead head) {
            int[] col = {0};
            head.accept(new AbsExtensionVisitor() {
              @Override
              public void visit(TableCell cell) {
                col[0]++;
              }
            });

            columns = new Table[col[0]];
            for (int i = 0; i < columns.length; i++) {
              if (i > 0) layoutTable.image().color(style.lineColor).width(3).growY().pad(0);
              columns[i] = layoutTable.table().get();
            }
            super.visit(head);
          }

          @Override
          public void visit(TableRow tableRow) {
            r = !r;
            currCol = 0;
            super.visit(tableRow);
            layoutTable.row();
          }

          @Override
          public void visit(TableCell cell) {
            int align = cell.getAlignment() == TableCell.Alignment.LEFT ? Align.left :
                cell.getAlignment() == TableCell.Alignment.RIGHT ? Align.right : Align.center;
            columns[currCol].table(r? style.tableBack1: style.tableBack2,
                t -> t.align(align).add(new Markdown(Markdown.this, cell, style)).align(align)
                .pad(style.tablePadVert, style.tablePadHor, style.tablePadVert, style.tablePadHor))
                .grow().pad(0);

            columns[currCol].row();
            currCol++;
          }
        });
        row();
        lineOff[0] += style.linesPadding;
        drawObjs.add(DrawTable.get(Markdown.this, layoutTable, rendOff[0], -lineOff[0]));
        updateTmpHeight(layoutTable.getPrefHeight());
        row();
        lineOff[0] += style.linesPadding;
      }

      private void updateTmpHeight(float h) {
        tmpHeight[0] = Math.max(tmpHeight[0], h);
      }

      private TextMirror makeStr(String str, Font font, Color color) {
        return makeStr(str, font, null, null, color);
      }

      private TextMirror makeStr(String str, Font font, Color color, String openUrl) {
        return makeStr(str, font, openUrl, null, color);
      }

      private TextMirror makeStr(String str, Font font, Drawable background, Color color) {
        return makeStr(str, font, null, background, color);
      }

      private TextMirror makeStr(String str, Font font, String openUrl, Drawable background, Color color) {
        float maxWidth = getWidth() <= font.getSpaceXadvance()*3? Float.MAX_VALUE: getWidth() - rendOff[0];
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

          tmp += glyph.xadvance*currScl*font.getData().scaleX;
          if (tmp > maxWidth){
            break;
          }
          width = tmp;
          index++;
        }

        TextMirror res = lastText;
        if (width > 0){
          rendOff[0] += background != null? background.getLeftWidth(): 0;
          if (res == null) res = new TextMirror(str.substring(0, index), font, color, rendOff[0], -lineOff[0], width, tmpHeight[0]);
          else res.sub = new TextMirror(str.substring(0, index), font, color, rendOff[0], -lineOff[0], width, tmpHeight[0]);

          drawObjs.add(openUrl != null?
              DrawClickable.get(Markdown.this, str.substring(0, index), font,
                  () -> Core.app.openURI(openUrl), new Tooltip(t -> t.table(style.board).get().add(openUrl)),
                  color, rendOff[0], -lineOff[0], currScl
              ):
              DrawStr.get(Markdown.this, str.substring(0, index), font, color, rendOff[0], -lineOff[0], currScl, background));
          rendOff[0] += width + (background != null? background.getRightWidth(): 0);
        }
        else if (res == null) res = new TextMirror("", font, color, rendOff[0], -lineOff[0], 0, tmpHeight[0]);
        else res.sub = new TextMirror("", font, color, rendOff[0], -lineOff[0], 0, tmpHeight[0]);

        lastText = res;

        if (index < str.length()){
          row();
          res.sub = makeStr(str.substring(index), font, openUrl, background, color);
        }

        return res;
      }

      private void makeCodeBox(String code) {
        padding += 4;
        lineOff[0] += style.linesPadding;
        row();
        float begin = lineOff[0];
        lineOff[0] += style.linesPadding*2;
        DrawPane pane = DrawPane.get(Markdown.this, code, style.codeFont, style.subTextColor, rendOff[0], -lineOff[0], style.maxCodeBoxHeight, style.codeBlockStyle);
        updateTmpHeight(pane.height() + style.linesPadding);
        drawObjs.add(pane);
        padding -= 4;
        row();
        lineOff[0] += style.linesPadding*2;
        drawObjs.add(DrawBoard.get(Markdown.this, style.codeBlockBack, 0, lineOff[0] - begin, -begin, -begin));

        DrawClickable c = DrawClickable.get(Markdown.this, Core.bundle.get("misc.copy"), Fonts.outline, () -> {
              Core.app.setClipboardText(code);
              Vars.ui.showInfoFade(Core.bundle.get("infos.copyToClip"));
            },
            null, style.subTextColor, pane.width() - 64, -begin - style.linesPadding*2 - 8, 1);
        drawObjs.add(c);
        Element e = c.getElem();
        e.color.a = 0.4f;
        e.hovered(() -> e.actions(Actions.alpha(1, 0.5f)));
        e.exited(() -> e.actions(Actions.alpha(0.4f, 0.5f)));
      }

      private void row() {
        prefWidth = Math.max(prefWidth, rendOff[0]);

        rendOff[0] = padding*style.font.getSpaceXadvance();
        lineOff[0] += tmpHeight[0] + style.linesPadding*currScl;

        prefHeight = lineOff[0];
        tmpHeight[0] = 0;
      }
    });

    prefWidth = Math.max(prefWidth, rendOff[0]);
    prefHeight += tmpHeight[0];

    prefInvalid = false;

    if(prefHeight != lastPrefHeight){
      lastPrefHeight = prefHeight;
      invalidateHierarchy();
    }

    drawObjs.sort((a, b) -> a.priority() - b.priority());
    clearChildren();
    for (DrawObj obj : drawObjs) {
      if (obj instanceof ActivityDrawer activity){
        Element element = activity.getElem();
        addChild(element);
        element.invalidate();
        element.setBounds(
            obj.offsetX,
            height + obj.offsetY - activity.height(),
            activity.width(),
            activity.height()
        );
        element.validate();
      }
    }
  }

  @Override
  public float getPrefWidth() {
    if (prefInvalid) layout();
    return prefWidth;
  }

  @Override
  public float getPrefHeight() {
    if (prefInvalid) layout();
    return prefHeight;
  }

  @Override
  public void draw() {
    validate();
    for (DrawObj obj : drawObjs) {
      Draw.color(Color.white);
      Draw.alpha(parentAlpha);
      obj.draw();
    }
    super.draw();
  }

  public static class MarkdownStyle {
    public Font font, codeFont, emFont, strongFont, subFont;
    public Color textColor, emColor, subTextColor, lineColor, linkColor;
    public float linesPadding, maxCodeBoxHeight, tablePadHor, tablePadVert;
    public Drawable board, codeBack, codeBlockBack, tableBack1, tableBack2, curtain;
    public ScrollPane.ScrollPaneStyle codeBlockStyle;
    public Drawable[] listMarks;
  }

  public abstract static class DrawObj implements Pool.Poolable {
    static final Color tmp1 = new Color(), tmp2 = new Color();

    protected Markdown parent;
    protected float offsetX, offsetY;

    abstract void draw();

    public int priority() {
      return 0;
    }

    @Override
    public void reset() {
      offsetX = offsetY = 0;
    }

    void free() {
      Pools.free(this);
    }
  }

  public interface ActivityDrawer {
    Element getElem();

    float width();

    float height();
  }
}
