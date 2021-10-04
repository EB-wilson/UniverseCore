package universeCore;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.world.Block;
import universeCore.override.dialogs.UncDatabaseDialog;
import universeCore.util.handler.CategoryHandler;
import universeCore.util.animLayout.CellActions;

import static mindustry.Vars.ui;

public class UncCore extends Mod{
  /**类型处理工具实例*/
  public static CategoryHandler categories = new CategoryHandler();
  /**单元格动画控制器实例*/
  public static CellActions cellActions = new CellActions();
  
  public UncCore(){
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
    
    Log.info("[Universe Core] core loading");
  }
  
  @Override
  public void init(){
    ui.database = new UncDatabaseDialog();
  }
}
