package universeCore.world.particles;

import arc.func.Cons;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;

/**粒子实体的偏转操作类，提供了一些偏转策略，可组进行多次偏转
 * @author EBwilson*/
public class Deflect{
  protected Seq<Cons<Particle>> deflect = new Seq<>();
  
  /**对指定的目标粒子进行偏转*/
  public void doDeflect(Particle target){
    for(Cons<Particle> cons : deflect){
      cons.get(target);
    }
  }
  
  public Deflect setDest(Vec2 dest){
    return setDest(dest, 0.035f, false);
  }
  
  public Deflect setDest(float x, float y){
    return setDest(new Vec2(x, y));
  }
  
  /**设置粒子的移动目的地，设置后粒子的朝向无论运动如何，运动方向都会向目的地靠近*/
  @SuppressWarnings("all")
  public Deflect setDest(Vec2 dest, float deflection, boolean linkLast){
    deflect.add(e -> {
      float from = e.speed.angle();
      float to = Tmp.v1.set(dest.x, dest.y).sub(e.x, e.y).angle();
      float r = to - from;
      r = r > 180? r-360: r < -180? r+360: r;
      e.speed.rotate(r*deflection*Time.delta);
    });
    return this;
  }
}
