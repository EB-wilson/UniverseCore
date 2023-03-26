package universecore.world.particles.models;

import universecore.world.particles.Particle;
import universecore.world.particles.ParticleModel;

public class DrawDefaultTrailParticle extends ParticleModel {
  @Override
  public void drawTrail(Particle particle) {
    for(Particle.Cloud c: particle){
      c.draw();
    }
  }
}
