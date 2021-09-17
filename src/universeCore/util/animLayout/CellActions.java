package universeCore.util.animLayout;

import arc.struct.IntMap;
import arc.util.Log;

public class CellActions{
  protected int idCounter = 0;
  protected CellAction currentAction;
  protected final IntMap<CellAction> actions = new IntMap<>();
  
  public void update(){
    if(actions.size == 0) return;
    for(IntMap.Entry<CellAction> action: actions){
      action.value.update();
      currentAction = action.value;
      if(action.value.isFinally()){
        action.value.reset();
        actions.remove(action.key);
        currentAction = null;
      }
    }
  }
  
  public boolean acting(CellAction act){
    return actions.containsValue(act, true);
  }
  
  public int add(CellAction action){
    while(actions.get(idCounter) != null) idCounter++;
    actions.put(idCounter, action);
    return idCounter;
  }
  
  public void add(int id, CellAction act){
    actions.put(id, act);
  }
  
  public void remove(int id){
    actions.remove(id);
  }
}