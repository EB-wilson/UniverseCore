package universecore.components.blockcomp;

import arc.util.Time;
import mindustry.world.Tile;
import universecore.annotations.Annotations;

import java.util.WeakHashMap;

/**可（被）替换覆盖放置的方块组件，这使得此方块可以在覆盖放置时传递信息，将旧方块传递给新的方块
 *
 * @since 1.5
 * @author EBwilson*/
public interface ReplaceBuildComp extends BuildCompBase {
  WeakHashMap<Tile, ReplaceBuildComp> replaceEntry = new WeakHashMap<>();

  @Annotations.MethodEntry(entryMethod = "onRemoved", insert = Annotations.InsertPosition.HEAD)
  default void onRemoving(){
    Tile tile = getTile();
    replaceEntry.put(tile, this);
    Time.runTask(1, () -> {
      if (replaceEntry.get(tile) == this){
        replaceEntry.remove(tile);
      }
    });
  }

  @Annotations.MethodEntry(entryMethod = "init", paramTypes = {"mindustry.world.Tile", "mindustry.game.Team", "boolean", "int"})
  default void buildInitialized(){
    ReplaceBuildComp old = replaceEntry.get(getTile());
    if (old == null) return;
    onReplaced(old);
    replaceEntry.remove(getTile());
  }

  /**在方块放置并覆盖了一个有效的可替换方块时调用，传入被覆盖的方块
   *
   * @param old 原本位于这个位置上的方块
   * */
  void onReplaced(ReplaceBuildComp old);
}
