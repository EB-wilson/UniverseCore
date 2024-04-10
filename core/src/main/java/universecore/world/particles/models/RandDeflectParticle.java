package universecore.world.particles.models;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.noise.Noise;
import universecore.world.particles.Particle;
import universecore.world.particles.ParticleModel;

public class RandDeflectParticle extends ParticleModel{
  public static final String DEFLECT_ANGLE = "deflectAngle";
  public static final String STRENGTH = "strength";

  public float strength = 1;
  public float deflectAngle = 45;

  public void deflect(Particle p) {
    float angle = Tmp.v1.set(p.speed).scl(-1.0F).angle();
    float scl = Mathf.clamp(p.speed.len() / p.defSpeed*Time.delta*p.getVar(STRENGTH, strength));
    Tmp.v2.set(p.speed).setAngle(angle + Noise.noise(p.x, p.y, 0.01f, 6.7f)*p.getVar(DEFLECT_ANGLE, this.deflectAngle)).scl(scl);
    p.speed.add(Tmp.v2);
  }
}
