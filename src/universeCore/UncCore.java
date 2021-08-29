package universeCore;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.world.Block;
import universeCore.override.dialogs.SglDatabaseDialog;
import universeCore.util.handler.CategoryHandler;

import static mindustry.Vars.ui;

public class UncCore extends Mod{
  public static CategoryHandler categories = new CategoryHandler();
  
  public UncCore(){
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
    ui.database = new SglDatabaseDialog();
  }
}
