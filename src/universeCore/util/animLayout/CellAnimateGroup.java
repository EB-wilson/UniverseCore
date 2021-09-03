package universeCore.util.animLayout;

import arc.struct.Queue;

public class CellAnimateGroup extends CellAction{
  protected final Queue<CellAction> actions = new Queue<>();
  protected final Queue<Runnable> afterHandle = new Queue<>();
  
  public CellAction current;
  public Runnable currentHandle;
  public CellAnimateGroup(Object... actions){
    duration = 1;
    
    for(int i=0; i<actions.length; i++){
      if(i + 1 < actions.length && actions[i] instanceof CellAction && actions[i + 1] instanceof Runnable){
        this.actions.addLast((CellAction)actions[i]);
        afterHandle.addLast((Runnable)actions[i+1]);
        i++;
      }
      else if((i + 1 >= actions.length && actions[i] instanceof CellAction) ||
        (i + 1 < actions.length && actions[i] instanceof CellAction && actions[i + 1] instanceof CellAction)){

        this.actions.addLast((CellAction)actions[i]);
        afterHandle.addLast(() -> {});
      }
    }
  }

  public void add(CellAction action, Runnable handle){
    actions.addLast(action);
    afterHandle.addLast(handle);
  }

  @Override
  public void update(){
    if(current == null || current.isFinally()){
      if(actions.size > 0){
        current = actions.removeFirst();
        currentHandle = afterHandle.removeFirst();
      }
      if(currentHandle != null){
        currentHandle.run();
        currentHandle = null;
      }
    }
    if(current != null)current.update();
  }

  @Override
  public boolean isFinally(){
    return actions.size == 0 && current.isFinally();
  }

  @Override
  public void action(){
  }
}