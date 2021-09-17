package universeCore.util.animLayout;

import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Log;

public class CellAnimateGroup extends CellAction{
  protected final Seq<CellAction> actions = new Seq<>();
  protected final Seq<Runnable> afterHandle = new Seq<>();
  
  protected int index = 0;
  protected boolean isFinally;
  
  public CellAction current;
  public Runnable currentHandle;
  
  public CellAnimateGroup(){
    duration = 1;
  }
  
  public CellAnimateGroup(Object... actions){
    this();
    
    for(int i=0; i<actions.length; i++){
      if(i + 1 < actions.length && actions[i] instanceof CellAction && actions[i + 1] instanceof Runnable){
        this.actions.add((CellAction)actions[i]);
        afterHandle.add((Runnable)actions[i+1]);
        i++;
      }
      else if((i + 1 >= actions.length && actions[i] instanceof CellAction) ||
        (i + 1 < actions.length && actions[i] instanceof CellAction && actions[i + 1] instanceof CellAction)){

        this.actions.add((CellAction)actions[i]);
        afterHandle.add(() -> {});
      }
    }
  }
  
  public int getCurrIndex(){
    return index - 1;
  }
  
  public void restart(){
    jump(0);
  }
  
  public void jump(int index){
    if(index >= actions.size) throw new IndexOutOfBoundsException(index);
    this.index = index;
    current = actions.get(index);
    currentHandle = afterHandle.get(index);
    
    for(int i=index; i<actions.size; i++){
      actions.get(i).reset();
    }
  }

  public void add(CellAction action, Runnable handle){
    actions.add(action);
    afterHandle.add(handle);
  }
  
  public void add(CellAction action){
    add(action, () -> {});
  }

  @Override
  public void update(){
    if(current == null || current.isFinally()){
      if(currentHandle != null){
        currentHandle.run();
        currentHandle = null;
      }
      
      if(index < actions.size){
        current = actions.get(index);
        currentHandle = afterHandle.get(index);
        index++;
      }
      else{
        current = null;
        isFinally = true;
      }
    }
    else current.update();
  }

  @Override
  public boolean isFinally(){
    return isFinally;
  }
  
  @Override
  public void reset(){
    isFinally = false;
    restart();
  }
  
  @Override
  public void action(){
  }
}