package universecore.world.particles.models;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import universecore.world.particles.Particle;
import universecore.world.particles.ParticleModel;

public class RandDeflectParticle extends ParticleModel{
  public float strength = 1;
  public float deflectAngle = 45;

  @Override
  public void deflect(Particle p){
    float angle = Tmp.v1.set(p.speed).scl(-1).angle();
    float scl = Mathf.clamp(p.speed.len()/p.defSpeed*Time.delta*strength);
    Tmp.v2.set(p.speed).setAngle(angle + Mathf.random(-deflectAngle, deflectAngle)).scl(scl);
    p.speed.add(Tmp.v2);
  }
}
