package universecore.world.particles.models;

import arc.graphics.Color;
import arc.math.Interp;
import arc.math.Mathf;
import universecore.world.particles.Particle;
import universecore.world.particles.ParticleModel;

public class SizeVelRelatedParticle extends ParticleModel {
  public float finalThreshold = 0.25f;
  public float fadeThreshold = 0.03f;
  public Interp sizeInterp = Interp.linear;

  @Override
  public boolean isFinal(Particle p){
    return p.speed.len() <= finalThreshold;
  }

  @Override
  public Color trailColor(Particle p){
    return null;
  }

  @Override
  public float currSize(Particle p){
    return p.defSize*sizeInterp.apply(Mathf.clamp(p.speed.len()/p.defSpeed));
  }

  @Override
  public boolean isFaded(Particle p, Particle.Cloud cloud){
    return cloud.size < fadeThreshold;
  }
}
