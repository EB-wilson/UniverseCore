package universeCore.util.animLayout;

import arc.struct.Seq;

public class CellActions{
  protected final Seq<CellAction> actions = new Seq<>();
  
  public void update(){
    if(actions.size == 0) return;
    for(int i=0; i<actions.size; i++){
      CellAction action = actions.get(i);
      action.update();
      if(action.isFinally()){
        actions.remove(i);
        i--;
      }
    }
  }
  
  public void add(CellAction action){
    actions.add(action);
  }
  
  public void remove(CellAction action){
    actions.remove(action);
  }
}