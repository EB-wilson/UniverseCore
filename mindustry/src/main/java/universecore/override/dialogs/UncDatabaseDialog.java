package universecore.override.dialogs;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.ClickListener;
import arc.scene.event.HandCursorListener;
import arc.scene.ui.Image;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Time;
import dynamilize.DynamicClass;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.dialogs.DatabaseDialog;
import universecore.UncCore;
import universecore.util.UncContentType;

/**重写database对话框以重新排序类型（鬼知道臭猫什么时候才能把读写机制改一改）
 * @author EBwilson
 * @since 1.0*/
@SuppressWarnings("unchecked")
public class UncDatabaseDialog{
  private static final DynamicClass UncDatabase = DynamicClass.get("UncDatabase");
  private static final ObjectSet<UnlockableContent> hiddenContents = new ObjectSet<>();

  static {
    UncDatabase.setFunction("<init>", (s, su, a) -> {
      s.<DatabaseDialog>castGet().clearListeners();
    });

    UncDatabase.setFunction("show", (s, su, a) -> {
      su.invokeFunc("show", a);

      DatabaseDialog self = s.castGet();
      self.cont.clear();

      Table table = new Table();
      table.margin(20);
      ScrollPane pane = new ScrollPane(table);

      Seq<Content>[] allContent = new Seq[UncContentType.displayContentList.length];

      for(int i = 0; i<UncContentType.displayContentList.length; i++){
        allContent[i] = Vars.content.getBy(UncContentType.displayContentList[i]);
      }

      for(int j = 0; j < allContent.length; j++){
        ContentType type = UncContentType.displayContentList[j];

        Seq<Content> array = allContent[j].select(c -> c instanceof UnlockableContent uc && !hiddenContents.contains(uc) && (!uc.isHidden() || (uc.techNode != null)));
        if(array.size == 0) continue;

        table.add("@content." + type.name() + ".name").growX().left().color(Pal.accent);
        table.row();
        table.image().growX().pad(5).padLeft(0).padRight(0).height(3).color(Pal.accent);
        table.row();
        table.table(list -> {
          list.left();

          int cols = Mathf.clamp((Core.graphics.getWidth() - 30) / (32 + 10), 1, 18);
          int count = 0;

          for(int i = 0; i < array.size; i++){
            UnlockableContent unlock = (UnlockableContent)array.get(i);

            Image image = unlocked(unlock) ? new Image(unlock.uiIcon).setScaling(Scaling.fit) : new Image(Icon.lock, Pal.gray);
            list.add(image).size(8 * 4).pad(3);
            ClickListener listener = new ClickListener();
            image.addListener(listener);
            if(!Vars.mobile && unlocked(unlock)){
              image.addListener(new HandCursorListener());
              image.update(() -> image.color.lerp(!listener.isOver() ? Color.lightGray : Color.white, Mathf.clamp(0.4f * Time.delta)));
            }

            if(unlocked(unlock)){
              image.clicked(() -> {
                if(Core.input.keyDown(KeyCode.shiftLeft) && Fonts.getUnicode(unlock.name) != 0){
                  Core.app.setClipboardText((char)Fonts.getUnicode(unlock.name) + "");
                  Vars.ui.showInfoFade("@copied");
                }else{
                  Vars.ui.content.show(unlock);
                }
              });
              image.addListener(new Tooltip(t -> t.background(Tex.button).add(unlock.localizedName)));
            }

            if((++count) % cols == 0){
              list.row();
            }
          }
        }).growX().left().padBottom(10);
        table.row();
      }

      self.cont.add(pane);
      return self;
    });
  }

  public static void hide(UnlockableContent content){
    hiddenContents.add(content);
  }

  public static DatabaseDialog make() {
    return UncCore.classes.getDynamicMaker().newInstance(DatabaseDialog.class, UncDatabase).castGet();
  }
  
  private static boolean unlocked(UnlockableContent content){
    return (!Vars.state.isCampaign() && !Vars.state.isMenu()) || content.unlocked();
  }
}
