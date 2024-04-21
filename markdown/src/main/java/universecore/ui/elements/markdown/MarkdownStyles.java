package universecore.ui.elements.markdown;

import arc.Core;
import arc.freetype.FreeTypeFontGenerator;
import arc.graphics.Color;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.Lines;
import arc.scene.style.BaseDrawable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Scl;
import arc.util.Tmp;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

import static mindustry.gen.Tex.paneLeft;
import static mindustry.gen.Tex.whiteui;

public class MarkdownStyles {
  public static Markdown.MarkdownStyle defaultMD(Font mono){
    return new Markdown.MarkdownStyle(){{
      font = subFont = Fonts.def;
      codeFont = mono;
      FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Core.files.internal("fonts/font.woff"));
      strongFont = gen.generateFont(new FreeTypeFontGenerator.FreeTypeFontParameter(){{
        size = (int) Scl.scl(19);
        borderWidth = Scl.scl(0.3f);
        shadowOffsetY = 2;
        incremental = true;
        borderColor = color;
      }});
      emFont = Fonts.def;

      textColor = Color.white;
      emColor = Pal.accent;
      subTextColor = Color.lightGray;
      lineColor = Color.gray;
      linkColor = Pal.place;

      linesPadding = 5;
      maxCodeBoxHeight = 400;
      tablePadHor = 14;
      tablePadVert = 10;
      paragraphPadding = 14;

      board = paneLeft;
      codeBack = ((TextureRegionDrawable) whiteui).tint(Tmp.c1.set(Pal.darkerGray).a(0.7f));
      codeBack.setLeftWidth(4);
      codeBack.setRightWidth(4);
      codeBlockBack = ((TextureRegionDrawable) whiteui).tint(Tmp.c1.set(Pal.darkerGray));
      codeBlockStyle = Styles.smallPane;

      tableBack1 = ((TextureRegionDrawable) whiteui).tint(Tmp.c1.set(Pal.darkerGray).a(0.7f));
      tableBack2 = ((TextureRegionDrawable) whiteui).tint(Tmp.c1.set(Color.gray).a(0.7f));

      curtain = ((TextureRegionDrawable) whiteui).tint(Pal.darkerGray);

      listMarks = new Drawable[]{
          new BaseDrawable(){
            @Override
            public void draw(float x, float y, float width, float height) {
              Fill.square(x + width/2, y + height/2, width*0.25f, 45f);
            }
          },
          new BaseDrawable(){
            @Override
            public void draw(float x, float y, float width, float height) {
              Fill.circle(x + width/2, y + height/2, width*0.3f);
            }
          },
          new BaseDrawable(){
            @Override
            public void draw(float x, float y, float width, float height) {
              Lines.stroke(1);
              Lines.circle(x + width/2, y + height/2, width*0.3f);
            }
          }
      };
    }};
  }
}
