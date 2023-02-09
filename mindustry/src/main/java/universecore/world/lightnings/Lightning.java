package universecore.world.lightnings;

import arc.func.Cons2;
import arc.func.FloatFloatf;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
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
  private static final Vec2 last = new Vec2(), self = new Vec2(), next = new Vec2();

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

  boolean enclosed;

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
    result.enclosed = generator.isEnclosed();
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
  @SuppressWarnings("DuplicatedCode")
  public void draw(float x, float y){
    float lerp = this.lerp.get((Time.time - startTime)/lifeTime);

    for(int i = 2; i <= vertices.size(); i++){
      LightningVertex v1 = i - 3 >= 0? vertices.get(i - 3): enclosed? vertices.get(Mathf.mod(i - 3, vertices.size())): null,
          v2 = vertices.get(i - 2),
          v3 = vertices.get(i - 1),
          v4 = i < vertices.size()? vertices.get(i): enclosed? vertices.get(Mathf.mod(i, vertices.size())): null;

      float lastOffX, lastOffY;
      float nextOffX, nextOffY;

      if(!v2.valid) break;

      self.set(v3.x, v3.y).sub(v2.x, v2.y);

      if(v1 != null){
        last.set(v2.x, v2.y).sub(v1.x, v1.y);

        float aveAngle = (last.angle() + self.angle())/2;
        float off = width/2*lerp/Mathf.cosDeg(aveAngle - last.angle());

        lastOffX = Angles.trnsx(aveAngle + 90, off);
        lastOffY = Angles.trnsy(aveAngle + 90, off);
      }
      else{
        Tmp.v1.set(self).rotate90(1).setLength(width/2*lerp);
        lastOffX = Tmp.v1.x;
        lastOffY = Tmp.v1.y;
      }

      if(v4 != null){
        next.set(v4.x, v4.y).sub(v3.x, v3.y);
        float aveAngle = (self.angle() + next.angle())/2;
        float off = width/2*lerp/Mathf.cosDeg(aveAngle - self.angle());

        nextOffX = Angles.trnsx(aveAngle + 90, off);
        nextOffY = Angles.trnsy(aveAngle + 90, off);
      }
      else{
        Tmp.v1.set(self).rotate90(1).setLength(width/2*lerp);
        nextOffX = Tmp.v1.x;
        nextOffY = Tmp.v1.y;
      }

      lastOffX *= lerp;
      lastOffY *= lerp;
      nextOffX *= lerp;
      nextOffY *= lerp;

      float orgX = x + v2.x, orgY = y + v2.y;

      Tmp.v1.set(self).scl(v2.progress);
      if(v2.isStart){
        float l = v2.progress;
        Fill.tri(
            orgX, orgY,
            orgX + Tmp.v1.x + nextOffX*l,
            orgY + Tmp.v1.y + nextOffY*l,
            orgX + Tmp.v1.x - nextOffX*l,
            orgY + Tmp.v1.y - nextOffY*l
        );
      }
      else if(v3.isEnd){
        float l = 1 - v2.progress;
        Fill.quad(
            orgX + lastOffX,
            orgY + lastOffY,
            orgX - lastOffX,
            orgY - lastOffY,
            orgX + Tmp.v1.x - nextOffX*l,
            orgY + Tmp.v1.y - nextOffY*l,
            orgX + Tmp.v1.x + nextOffX*l,
            orgY + Tmp.v1.y + nextOffY*l
        );
      }
      else{
        Fill.quad(
            orgX + lastOffX,
            orgY + lastOffY,
            orgX - lastOffX,
            orgY - lastOffY,
            orgX + Tmp.v1.x - nextOffX,
            orgY + Tmp.v1.y - nextOffY,
            orgX + Tmp.v1.x + nextOffX,
            orgY + Tmp.v1.y + nextOffY
        );
      }

      Drawf.light(
          orgX, orgY,
          orgX + Tmp.v1.x, orgY + Tmp.v1.y,
          width*32,
          Draw.getColor(),
          Draw.getColor().a
      );

      v2.draw(x, y);
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
    enclosed = false;
    lerp = null;
    lengthMargin = 0;
    startTime = 0;
    clipSize = 0;
    trigger = null;
  }
}
