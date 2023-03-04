package universecore.world.particles;

import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.util.pooling.Pools;

public class ParticleModel{
  public Color trailColor = Color.white;


  public Particle create(float x, float y, float sx, float sy, float size){
    Particle ent = Pools.obtain(Particle.class, Particle::new);
    ent.x = x;
    ent.y = y;
    ent.startPos.set(x, y);
    ent.speed = new Vec2(sx, sy);
    ent.defSpeed = ent.speed.len();
    ent.defSize = size;
    ent.size = currSize(ent);

    ent.model = this;
    ent.add();
    
    return ent;
  }

  public void draw(Particle p){

  }

  public void updateTrail(Particle p, Particle.Cloud c){

  }

  public void update(Particle p){

  }

  public void deflect(Particle p){

  }

  public boolean isFinal(Particle p){
    return p.speed.len() <= 0.03f;
  }

  public Color trailColor(Particle p){
    return trailColor;
  }

  public float currSize(Particle p){
    return p.defSize*(p.speed.len()/p.defSpeed);
  }

  public boolean isFaded(Particle p, Particle.Cloud cloud){
    return cloud.size < 0.03f;
  }
}
