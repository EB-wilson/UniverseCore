package universecore.util.animate;

import arc.util.Time;
import arc.func.Floatf;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.scene.Element;
import universecore.math.Functions;

public abstract class CellAction{
  protected static final Element refresher = new Element();
  
  /**线性时间插值，以均匀增量增长，范围[0，1]*/
  protected float time;
  /**动作的总时长*/
  protected float duration;
  /**动画进度，由时间插值计算得到，范围为[0，1]*/
  protected float progress;
  
  /**执行该动作的目标单元格*/
  protected Cell<?> cell;
  /**该cell所在的table*/
  protected Table table;
  /**计算动画进度的函数，输入时间线性插值*/
  public Floatf<Float> progHandler = f -> f;
  
  public CellAction(Cell<?> cell, Table table, float time){
    this.cell = cell;
    this.table = table;
    this.duration = time;
  }
  
  public CellAction(){}
  
  public void update(){
    if(cell == null || cell.get() == null || table == null){
      time = 1;
      return;
    }
    time = Math.min(time + Time.delta/duration, 1);
    
    progress = progHandler.get(time);
    action();
    
    //这里是因为需要刷新布局
    table.add(refresher).pad(0).size(0);
    table.removeChild(refresher);
  }
  
  public boolean isFinally(){
    return time >= 1;
  }
  
  public float progress(){
    return progress;
  }
  
  public float time(){
    return time*duration;
  }
  
  public void reset(){
    time = 0;
    progress = 0;
  }
  
  public abstract void action();
  
  /**使进度的速度进行渐变
   * @param scl 增长倍率
   * 当倍率大于0.5时，呈渐快趋势，小于0.5时呈渐慢趋势
   * 当倍率等于0时，呈线性增长。
   * 特别的，当倍率小于0时，delta返回值永远为1*/
  public final CellAction gradient(float scl){
    progHandler = f -> (float) Functions.lerp(0, 1, scl, f * duration);
    return this;
  }
}