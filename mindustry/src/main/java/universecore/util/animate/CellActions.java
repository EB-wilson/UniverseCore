package universecore.util.animate;

import arc.struct.IntMap;

public class CellActions{
  protected int idCounter = 0;
  protected CellAction currentAction;
  protected boolean wait = true;
  protected final IntMap<CellAction> actions = new IntMap<>();
  
  public void update(){
    if(actions.size == 0) return;
    if(wait){
      wait = false;
      return;
    }
    for(IntMap.Entry<CellAction> action: actions){
      if(wait) continue;
      action.value.update();
      currentAction = action.value;
      if(action.value.isFinally()){
        action.value.reset();
        actions.remove(action.key);
        currentAction = null;
      }
    }
  }
  
  public void waitAction(){
    wait = true;
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
  
  public void clear(){
    actions.clear();
    wait = true;
  }
}