package universecore.world.particles;

import arc.struct.Seq;

public class MultiParticleModel extends ParticleModel{
  public Seq<ParticleModel> models = new Seq<>();

  public MultiParticleModel(ParticleModel... models){
    this.models.addAll(models);
  }

  @Override
  public void draw(Particle p){
    for(ParticleModel model: models){
      model.draw(p);
    }
  }

  @Override
  public void drawTrail(Particle c) {
    for (ParticleModel model : models) {
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

    for (ParticleModel model : models) {
      res = Math.min(model.currSize(p), res);
    }

    return res;
  }
}
