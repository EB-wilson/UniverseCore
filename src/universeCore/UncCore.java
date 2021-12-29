package universeCore;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.world.Block;
import universeCore.override.dialogs.UncDatabaseDialog;
import universeCore.util.handler.CategoryHandler;
import universeCore.util.animLayout.CellActions;
import universeCore.util.handler.ClassHandler;

import static mindustry.Vars.netClient;
import static mindustry.Vars.ui;

public class UncCore extends Mod{
  /**类型处理工具实例*/
  public static CategoryHandler categories = new CategoryHandler();
  /**单元格动画控制器实例*/
  public static CellActions cellActions = new CellActions();
  /**类处理程序*/
  public static ClassHandler classes = new ClassHandler();
  
  public UncCore(){
    if(Tmp.v1.x == 0){
      Log.info("[Universe Core] core loading");
  
      Events.run(EventType.Trigger.update, () -> {
        cellActions.update();
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
      
      Tmp.v1.set(1, 0);
    }
    else Log.info("[UniverseCore] core was loaded, skip this repeat load");
  }
  
  @Override
  public void init(){
    if(!Vars.net.server()) ui.database = new UncDatabaseDialog();
  }
}
