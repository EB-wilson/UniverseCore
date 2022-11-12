package universecore.world.lightnings;

import arc.func.Cons2;
import arc.func.FloatFloatf;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.graphics.Drawf;
import universecore.world.lightnings.generator.LightningGenerator;

import java.util.LinkedList;

/**
 * 单条闪电的存储容器，保存了闪电的起始时间还有闪电的顶点信息
 * 此类实例大量，应当复用
 *
 * @since 1.5
 * @author EBwilson
 */
public class Lightning implements Pool.Poolable{
  public final LinkedList<LightningVertex> vertices = new LinkedList<>();
  /**闪电的持续时间*/
  public float lifeTime;
  /**闪电被创建时的时间*/
  public float startTime;
  /**这道闪电的剪切尺寸，用于绘制时的画面裁切*/
  public float clipSize;
  /**闪电的宽度*/
  public float width;

  /**闪电的宽度插值函数，参数由1-0*/
  public FloatFloatf lerp;
  /**闪电的每一段的触发器，在任意一段闪电的部分生成完成时会各自调用一次，传入当前顶点和前一个顶点*/
  public Cons2<LightningVertex, LightningVertex> trigger;

  /**闪电的蔓延速度，若不设置将使用{@link Lightning#time}确定闪电完全出现的时间*/
  public float speed;
  /**闪电由产生到完全显现的时间，在{@link Lightning#speed}未设置的情况下有效*/
  public float time;
  public float counter, lengthMargin;

  float totalLength;

  int cursor;

  public static Lightning create(LightningGenerator generator, float width, float lifeTime, FloatFloatf lerp, float time, Cons2<LightningVertex, LightningVertex> trigger){
    return create(generator, width, lifeTime, lerp, time, 0, trigger);
  }

  public static Lightning create(LightningGenerator generator, float width, float lifeTime, FloatFloatf lerp, float time, float speed, Cons2<LightningVertex, LightningVertex> trigger){
    Lightning result = Pools.obtain(Lightning.class, Lightning::new);
    result.width = width;
    result.speed = speed;
    result.time = time;
    result.startTime = Time.time;
    result.lifeTime = lifeTime;
    result.lerp = lerp;
    result.trigger = trigger;

    generator.setCurrentGen(result);

    LightningVertex last = null;
    for(LightningVertex vertex: generator){
      result.vertices.add(vertex);
      if(last != null){
        result.totalLength += Mathf.len(vertex.x - last.x, vertex.y - last.y);
      }
      last = vertex;
    }
    result.clipSize = generator.clipSize();

    return result;
  }
  
  private Lightning(){}

  /**更新一次闪电状态*/
  public void update(){
    if(speed == 0 && time == 0 && cursor < vertices.size()){
      LightningVertex per = null;
      for(LightningVertex vertex: vertices){
        if(per != null){
          per.progress = 1;
          vertex.valid = true;
          if(trigger != null) trigger.get(per, vertex);
        }
        per = vertex;
      }
      cursor = vertices.size();
    }
    else{
      float increase = (speed == 0? vertices.size()/time: speed/totalLength)*Time.delta;

      while(increase > 0){
        if(cursor == 0){
          cursor++;
        }

        if(cursor >= vertices.size()) break;

        LightningVertex per = vertices.get(cursor - 1), curr = vertices.get(cursor);
        float delta = Math.min(increase, 1 - per.progress);
        per.progress += delta;
        increase -= delta;

        if(per.progress >= 1){
          curr.valid = true;
          if(trigger != null) trigger.get(per, curr);
          cursor++;
        }
      }
    }

    for(LightningVertex vertex: vertices){
      if(!vertex.isEnd && !vertex.isStart && vertex.valid) vertex.update();
    }
  }

  /**绘制这道闪电
   *
   * @param x 绘制闪电的原点x坐标
   * @param y 绘制闪电的原点y坐标
   * */
  public void draw(float x, float y){
    LightningVertex last = null;

    float lerp = this.lerp.get((Time.time - startTime)/lifeTime);

    for(LightningVertex vertex: vertices){
      if(last != null){
        if(!last.valid) break;

        float hstroke = width/2f*lerp;

        Tmp.v1.set(Tmp.v2.set(vertex.x - last.x, vertex.y - last.y)).rotate90(1).setLength(hstroke);
        Tmp.v2.scl(last.progress);

        float lastXAbs = x + last.x;
        float lastYAbs = y + last.y;
        if(last.isStart){
          Fill.tri(
              lastXAbs + Tmp.v2.x + Tmp.v1.x,
              lastYAbs + Tmp.v2.y + Tmp.v1.y,
              lastXAbs + Tmp.v2.x - Tmp.v1.x,
              lastYAbs + Tmp.v2.y - Tmp.v1.y,
              lastXAbs,
              lastYAbs
          );
        }
        else{
          if(vertex.isEnd){
            Fill.tri(
                lastXAbs + Tmp.v1.x,
                lastYAbs + Tmp.v1.y,
                lastXAbs - Tmp.v1.x,
                lastYAbs - Tmp.v1.y,
                lastXAbs + Tmp.v2.x,
                lastYAbs + Tmp.v2.y
            );
          }
          else{
            Lines.stroke(width*lerp);
            Lines.line(lastXAbs, lastYAbs, lastXAbs + Tmp.v2.x, lastYAbs + Tmp.v2.y);
          }
        }
        Drawf.light(lastXAbs, lastYAbs, x + vertex.x, y + vertex.y, hstroke*4.5f, Draw.getColor(), 0.7f*lerp);
        if(vertex.valid) vertex.draw(x, y);
      }

      last = vertex;
    }
  }

  @Override
  public void reset(){
    for(LightningVertex vertex: vertices){
      Pools.free(vertex);
    }
    vertices.clear();
    counter = 0;
    width = 0;
    speed = 0;
    time = 0;
    cursor = 0;
    lifeTime = 0;
    lerp = null;
    lengthMargin = 0;
    startTime = 0;
    clipSize = 0;
    trigger = null;
  }
}
