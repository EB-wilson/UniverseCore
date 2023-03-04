package universecore.world.particles.models;

import arc.util.Time;
import universecore.world.particles.Particle;
import universecore.world.particles.ParticleModel;

public class TrailFadeParticle extends ParticleModel{
  public float trailFade = 0.075f;

  @Override
  public void updateTrail(Particle p, Particle.Cloud c){
    c.size -= trailFade*Time.delta;
  }
}
