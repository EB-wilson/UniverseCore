package universecore.util.animate;

import arc.graphics.Color;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;

public class CellChangeColorAction extends CellAction{
  public Color origColor;
  public Color setColor;
  
  public Color currColor;
  
  public CellChangeColorAction(Cell<?> cell, Table table, Color origColor, Color setColor, float time){
    super(cell, table, time);
    this.setColor = setColor;
    this.origColor = currColor = origColor;
  }
  
  public CellChangeColorAction(Cell<?> cell, Table table, Color setColor, float time){
    this(cell, table, cell.get().color.cpy(), setColor, time);
  }
  
  @Override
  public void action(){
    currColor = origColor.cpy().lerp(setColor, progress);
    
    cell.get().setColor(currColor);
  }
}