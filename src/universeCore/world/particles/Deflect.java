package universeCore.world.particles;

import arc.func.Cons;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;

public class Deflect{
  protected Seq<Cons<Particle>> deflect = new Seq<>();
  
  public void doDeflect(Particle target){
    for(Cons<Particle> cons : deflect){
      cons.get(target);
    }
  }
  
  public Deflect setDest(float x, float y){
    return setDest(x, y, 0.035f, false);
  }
  
  @SuppressWarnings("all")
  public Deflect setDest(float x, float y, float deflection, boolean linkLast){
    Vec2 dest = new Vec2(x, y);
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
