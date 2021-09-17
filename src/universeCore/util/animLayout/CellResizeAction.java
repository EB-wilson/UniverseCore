package universeCore.util.animLayout;

import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Log;

public class CellResizeAction extends CellAction{
  public float origWidth, origHeight;
  public float setWidth, setHeight;
  
  public float currWidth, currHeight;
  
  public CellResizeAction(Cell<?> cell, Table table, float originW, float originH, float setWidth, float setHeight, float time){
    super(cell, table, time);
    this.setWidth = setWidth;
    this.setHeight = setHeight;
    currWidth = origWidth = originW;
    currHeight = origHeight = originH;
  }
  
  public CellResizeAction(Cell<?> cell, Table table, float setWidth, float setHeight, float time){
    this(cell, table, cell.prefWidth(), cell.prefHeight(), setWidth, setHeight, time);
  }
  
  @Override
  public void action(){
    if(origWidth != setWidth) currWidth = origWidth + (setWidth - origWidth)*progress;
    if(origHeight != setHeight) currHeight = origHeight + (setHeight - origHeight)*progress;
    
    //cell的size很奇怪，即使一边长度设为原来的值也会出现干扰(坐标压缩了)
    //所以此处区分边进行操作
    if(currWidth == origWidth && currHeight != origHeight){
      cell.height(currHeight);
    }
    else if(currWidth != origWidth && currHeight == origHeight){
      cell.width(currWidth);
    }
    else if(currWidth != origWidth && currHeight != origHeight){
      cell.size(currWidth, currHeight);
    }
  }
}