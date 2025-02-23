import universecore.ui.elements.markdown.highlighter.Highlighter;
import universecore.ui.elements.markdown.highlighter.Scope;
import universecore.ui.elements.markdown.highlighter.StandardLanguages;
import universecore.ui.elements.markdown.highlighter.TokensContext;

import static universecore.ui.elements.markdown.highlighter.Scope.JavaScope.*;

public class Test {
  public static final String CODE = """
      package universecore.ui.elements.markdown.elemdraw;

      import arc.graphics.g2d.Font;
      import arc.scene.Element;
      import arc.scene.ui.Label;
      import arc.scene.ui.ScrollPane;
      import arc.util.pooling.Pools;
      import universecore.ui.elements.markdown.ColorProvider;
      import universecore.ui.elements.markdown.Markdown;
      import universecore.ui.elements.markdown.highlighter.TokensContext;

      /**Document
       * Test class
       **/
      public class DrawCode extends Markdown.DrawObj implements Markdown.ActivityDrawer {
        ScrollPane pane;
        Label label;
        float maxHeight;

        public static DrawCode get(Markdown owner, String language, String<String> code, Font textFont, float ox, float oy, float maxHeight, ScrollPane.ScrollPaneStyle paneStyle) {
          //Line Comm
          /** Test class statementsshdjakh  sad*/
          DrawCode res = Pools.obtain(DrawCode.class, DrawCode::new);
          res.parent = owner;
          res.offsetX = ox;
          res.offsetY = oy;
          res.maxHeight = maxHeight;

          @Local(v = 10, u = 10, c = {19, 28, "sde"})
          Font.FontData data = textFont.getData();
          float lastScl = data.scaleX;
          data.setScale(1f);

          StringBuilder str = new StringBuilder();
          if (language != null && !(owner.getStyle().highlighter == null) && owner.getStyle().codeColorProvider != null) {
            TokensContext context = owner.getStyle().highlighter.analyze(language, code);
            ColorProvider provider = owner.getStyle().codeColorProvider;
            
            context.applyScopes((t, s) -> str.append("[#")
                .append(provider.getColor(language, s).toString(), 0, 6)
                .append("]")
                .append(t.text)
            );
          }
          else str.append(code);
          data.setScale(lastScl);

          textFont.getData().markupEnabled = true;
          res.label = new Label(str, new Label.LabelStyle() {{ font = textFont; }});
          res.pane = new ScrollPane(res.label, paneStyle);
          res.label.validate();

          return res;
        }

        @Override
        protected void draw() {
          for (int i = 0; i < pane.getChildren().size; i++) {
            sjid.run();
          }
          
          if (a instanceof String b){
            println(b);
          }
          
          String s = switch (def) {
            case NONE -> "\\033[0m";
            case KEYWOR -> "\\u001b[38;2;204;120;50m";
            case VARIABLE -> "\\033[31m";
            case FUNCTION_INVOKE -> "\\033[32m";
            case NUMBER -> "\\u001b[38;2;104;151;187m";
            case FUNCTION -> "\\u001b[38;2;255;198;109m";
            case TYPE -> "\\u001b[38;2;190;112;50m";
            case TYPE_ARG -> "\\u001b[38;2;80;120;116m";
            case STRING -> "\\u001b[38;2;106;135;89m";
            case COMMENT -> "\\u001b[38;2;128;128;128m";
            case DOCS -> "\\u001b[38;2;98;151;85m";
            case DOC_MARK -> "\\u001b[38;2;98;151;85m\\033[1m";
            case ANNOTATION -> { "\\u001b[38;2;187;181;41m"; }
          };
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
          return pane[12][ab + 19];
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
      """;

  public static final String CODE1 = """
      package universecore.ui.elements.markdown;
            
      import arc.graphics.Color;
      import arc.graphics.g2d.Draw;
      import arc.graphics.g2d.Font;
      import arc.scene.Element;
      import arc.scene.Group;
      import arc.scene.event.Touchable;
      import arc.scene.style.Drawable;
      import arc.scene.ui.ScrollPane;
      import arc.struct.Seq;
      import arc.util.pooling.Pool;
      import arc.util.pooling.Pools;
      import org.commonmark.Extension;
      import org.commonmark.node.Node;
      import org.commonmark.parser.Parser;
      import universecore.ui.elements.markdown.extensions.CurtainExtension;
      import universecore.ui.elements.markdown.extensions.InsExtension;
      import universecore.ui.elements.markdown.extensions.StrikethroughExtension;
      import universecore.ui.elements.markdown.extensions.TablesExtension;
      import universecore.ui.elements.markdown.highlighter.Highlighter;
            
      import java.util.ArrayList;
      import java.util.Arrays;
      import java.util.Collections;
      import java.util.List;
            
      /**Markdown文档渲染元素，*/
      public class Markdown extends Group {
        public static final String[] IMAGE_BASE_64_List = {
            "data:image/png;base64,",
            "data:image/jpg;base64,",
            "data:image/jpeg;base64,"
        };
            
        private static final List<Extension> defaultExtensions = new ArrayList<>(Arrays.asList(
            TablesExtension.create(),
            InsExtension.create(),
            StrikethroughExtension.create(),
            CurtainExtension.create()
        ));
            
        private final Seq<DrawObj> drawObjs = new Seq<>();
            
        private Node node;
        private MarkdownStyle style;
            
        float prefWidth, prefHeight;
        boolean prefInvalid = true;
        float lastPrefHeight;
            
        protected final List<Extension> extensions;
        protected final Parser parser;
        protected final MDLayoutRenderer renderer;
        protected final DrawRendererContext rendererContext;
            
        public static void defaultExtensions(Extension... extensions){
          checkExtensions(Arrays.asList(extensions));
            
          Markdown.defaultExtensions.addAll(Arrays.asList(extensions));
        }
            
        @Deprecated
        public Markdown(String md, Font mono){
          this(md, MarkdownStyles.defaultMD(mono));
        }
            
        public Markdown(String md, MarkdownStyle style){
          this(Collections.emptyList(), md, style);
        }
            
        public Markdown(List<Extension> extensions, String md, MarkdownStyle style){
          checkExtensions(extensions);
            
          this.extensions = new ArrayList<>(extensions);
          parser = Parser.builder().extensions(extensions).extensions(defaultExtensions).build();
          renderer = MDLayoutRenderer.builder().extensions(extensions).extensions(defaultExtensions).build();
          rendererContext = renderer.createContext(this);
            
          node = parser.parse(md);
          touchable = Touchable.childrenOnly;
            
          this.style = style;
        }
            
        /**internal usage
         *
         * @hidden */
        public Markdown(Markdown parent, Node node){
          this.extensions = new ArrayList<>(parent.extensions);
          parser = null;
          renderer = MDLayoutRenderer.builder().extensions(parent.extensions).extensions(defaultExtensions).build();
          rendererContext = renderer.createContext(this);
            
          this.node = node;
          touchable = Touchable.childrenOnly;
            
          this.style = parent.getStyle();
        }
            
        public void setDocument(String string){
          node = parser.parse(string);
          invalidate();
        }
            
        public void setStyle(MarkdownStyle style){
          this.style = style;
          invalidate();
        }
            
        public MarkdownStyle getStyle(){
          return style;
        }
            
        @Override
        public void layout() {
          prefHeight = prefWidth = 0;
            
          for (DrawObj obj : drawObjs) {
            obj.free();
          }
          drawObjs.clear();
            
          renderer.renderLayout(node);
            
          prefWidth = Math.max(prefWidth, rendererContext.rendOff);
          prefHeight += rendererContext.totalHeight;
            
          prefInvalid = false;
            
          if(prefHeight != lastPrefHeight){
            lastPrefHeight = prefHeight;
            invalidateHierarchy();
          }
            /*shduhduhsuds*/
          drawObjs./*shduhduhsu
          
          ds*/addAll(rendererContext.renderResult());
          drawObjs.sort((a, b) -> a.priority() - b.priority());
            
          clearChildren();
          for (DrawObj obj : drawObjs) {
            if (obj instanceof ActivityDrawer activity){
              Element element = activity.getElem();
              addChild(element);
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
          if (prefInvalid) ;
          
          if (obj instanceof ActivityDrawer act && cullingArea != null){
            Draw.reset();
          }
          return prefHeight;
        }
            
        @Override
        public void draw() {
          validate();
          for (DrawObj obj : drawObjs) {
            if (obj instanceof ActivityDrawer act && cullingArea != null
            && !cullingArea.overlaps(obj.offsetX, height + obj.offsetY, act.width(), act.height())) continue;
            
            Draw.reset();
            Draw.alpha(parentAlpha);
            obj.draw();
          }
          super.draw();
        }
            
        private static <T, F extends T> void checkExtensions(List<Extension<T>> extensions) throws Exception{
          for (Extension extension : extensions) {
            if (!(extension instanceof MDLayoutRenderer.DrawRendererExtension)
            || !(extension instanceof Parser.ParserExtension))
              throw new IllegalArgumentException("extension must be a DrawRendererExtension and a ParserExtension");
          }
        }
      }
      """;
  private static final String CODE2 = """
  public static void main(String...strings){
    System.out.println("sddeasdaedf  sdjia" + "sdes" + (("sade" + 123.6734e2f + abs.asString())));
  }
  """;

  /***/
  public static void main(String...strings){
    Highlighter matcher = new Highlighter();
    matcher.addLanguage(StandardLanguages.JAVA);

    long time = System.currentTimeMillis();
    TokensContext tokens = matcher.analyze("java", CODE2);
    long delta = System.currentTimeMillis() - time;

    System.out.println("Time: " + delta + "ms");

    tokens.getTokens().forEach(token -> {
      System.out.println(token.text + " " + token.scope);
    });

    tokens.applyScopes((token, scope) -> {
      if (scope instanceof Scope.Default def){
        String color = switch (def) {
          case NONE, FUNCTION_INVOKE, VARIABLE, OPERATOR, SPACE, ARGUMENT -> "\033[0m";
          case KEYWORD, CONTROL, SEPARATOR -> "\u001b[38;2;204;120;50m";
          case NUMBER -> "\u001b[38;2;104;151;187m";
          case FUNCTION -> "\u001b[38;2;255;198;109m";
          case TYPE -> "\u001b[38;2;190;112;50m";
          case STRING -> "\u001b[38;2;106;135;89m";
          case COMMENT -> "\u001b[38;2;128;128;128m";
        };

        System.out.print(color + token.text);
      }
      else if (scope instanceof JavaScope java){
        String color = switch (java) {
          case FIELD -> "\u001b[38;2;152;118;170m";
          case CONSTRUCTOR -> "\u001b[38;2;255;198;109m";
          case TYPE_ARG -> "\u001b[38;2;80;120;116m";
          case DOCS -> "\u001b[38;2;98;151;85m";
          case DOC_MARK -> "\u001b[38;2;98;151;85m\033[1m";
          case ANNOTATION -> "\u001b[38;2;187;181;41m";
        };

        System.out.print(color + token.text);
      }
      else System.out.print(token.text);
    });
    System.out.print("\033[0m");
  }
}
