package universecore.world.particles;

import arc.graphics.Color;
import arc.util.Tmp;

public class MultiParticleModel extends ParticleModel{
  public ParticleModel[] models;

  public MultiParticleModel(ParticleModel... models){
    this.models = models;
  }

  @Override
  public void draw(Particle p){
    for(ParticleModel model: models){
      model.draw(p);
    }
  }

  @Override
  public void drawTrail(Particle c) {
    for (ParticleModel model: models) {
      model.drawTrail(c);
    }
  }

  @Override
  public void updateTrail(Particle p, Particle.Cloud c){
    for(ParticleModel model: models){
      model.updateTrail(p, c);
    }
  }

  @Override
  public void update(Particle p){
    for(ParticleModel model: models){
      if (model == null) break;
      model.update(p);
    }
  }

  @Override
  public void init(Particle p){
    for(ParticleModel model: models){
      model.init(p);
    }
  }

  @Override
  public Color trailColor(Particle p) {
    Tmp.c1.set(p.color);
    for (ParticleModel model: models) {
      Color c = model.trailColor(p);
      if (c == null) continue;
      Tmp.c1.mul(c);
    }
    return Tmp.c1;
  }

  @Override
  public void deflect(Particle p){
    for(ParticleModel model: models){
      model.deflect(p);
    }
  }

  @Override
  public boolean isFinal(Particle p){
    for(ParticleModel model: models){
      if(model.isFinal(p)) return true;
    }
    return false;
  }

  @Override
  public boolean isFaded(Particle p, Particle.Cloud cloud){
    for(ParticleModel model: models){
      if(model.isFaded(p, cloud)) return true;
    }
    return false;
  }

  @Override
  public float currSize(Particle p) {
    float res = Float.MAX_VALUE;

    for (ParticleModel model: models) {
      res = Math.min(model.currSize(p), res);
    }

    return res;
  }
}
