package universecore.world.particles.models;

import arc.func.Floatf;
import arc.func.Func;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import universecore.world.particles.Particle;
import universecore.world.particles.ParticleModel;

public class TargetMoveParticle extends ParticleModel{
  public Floatf<Particle> deflection = e -> 0.2f;
  public Func<Particle, Vec2> dest;

  @Override
  public void deflect(Particle e){
    float from = e.speed.angle();
    Vec2 dest = this.dest.get(e);
    float to = Tmp.v1.set(dest.x, dest.y).sub(e.x, e.y).angle();
    float r = to - from;
    r = r > 180? r-360: r < -180? r+360: r;
    e.speed.rotate(r*deflection.get(e)*Time.delta);
  }

  @Override
  public boolean isFinal(Particle e){
    Vec2 dest = this.dest.get(e);
    return Mathf.len(e.x - dest.x, e.y - dest.y) <= 2f;
  }
}
