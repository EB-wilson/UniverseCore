package universecore;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.scene.Group;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.world.Block;
import universecore.override.dialogs.UncDatabaseDialog;
import universecore.ui.fragments.SecondaryConfigureFragment;
import universecore.ui.styles.UncStyles;
import universecore.util.aspect.AspectManager;
import universecore.util.aspect.EntityAspect;
import universecore.util.aspect.triggers.EventControl;
import universecore.util.aspect.triggers.TriggerControl;
import universecore.util.handler.CategoryHandler;
import universecore.util.handler.ClassHandler;
import universecore.util.handler.FieldHandler;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

import java.util.Objects;

;

/**UniverseCore的mod主类，同时也是调用核心类，这里会保存各种可能会用到的默认实例以及许多必要实例
 * @author EBwilson*/
public class UncCore extends Mod{
  private static final ObjectMap<Class<? extends Mod>, ModInfo> referredMods = new ObjectMap<>();

  /**此mod内部名称*/
  public static final String coreName = "universe-core";

  /**mod项目地址*/
  public static final String coreGithubProject = "https://github.com/EB-wilson/UniverseCore";

  /**本模组的文件位置*/
  public static final Fi coreFile = Objects.requireNonNull(ModGetter.getModWithName(coreName)).file;

  public static ClassHandler classes;

  public static SecondaryConfigureFragment secConfig;
  
  /**方块类别处理工具实例*/
  public static CategoryHandler categories = new CategoryHandler();
  
  /**切面管理器实例*/
  public static AspectManager aspects = AspectManager.getDefault();

  static{
    aspects.addTriggerControl(new EventControl());
    aspects.addTriggerControl(new TriggerControl());

    signup(UncCore.class);
    classes = ImpCore.classes.getHandler(UncCore.class);
  }
  
  public UncCore(){
    Log.info("[Universe Core] core loading");

    Events.on(EventType.ClientLoadEvent.class, e -> {
      EntityAspect.Group.reset();
    });

    Time.run(0f, () -> {
      Events.on(EventType.UnlockEvent.class, event -> {
        if(event.content instanceof Block){
          categories.handleBlockFrag();
        }
      });

      Events.on(EventType.WorldLoadEvent.class, e -> {
        Core.app.post(categories::handleBlockFrag);
      });
    });

    UncStyles.load();
  }
  
  public static void signup(Class<? extends Mod> modClass){
    referredMods.put(modClass, ModGetter.getModWithClass(modClass));
  }
  
  @Override
  public void init(){
    if(!Vars.net.server()) {
      Vars.ui.database = UncDatabaseDialog.make();
      Group overlay = FieldHandler.getValueDefault(Vars.control.input, "group");
      FieldHandler.decache(Vars.control.input.getClass());
      secConfig = new SecondaryConfigureFragment();
      secConfig.build(overlay);
    }
    categories.init();
    Time.run(2, classes::finishGenerate);
  }
}