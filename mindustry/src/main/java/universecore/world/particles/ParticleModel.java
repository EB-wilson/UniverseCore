package universecore.world.particles;

import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.util.pooling.Pools;
import mindustry.graphics.Layer;

public class ParticleModel{
  public Color color = Color.white;
  public Color trailColor = Color.white;

  public Particle create(float x, float y, float sx, float sy, float size){
    return create(x, y, sx, sy, size, Layer.effect);
  }

  public Particle create(float x, float y, float sx, float sy, float size, float layer){
    Particle ent = Pools.obtain(Particle.class, Particle::new);
    ent.x = x;
    ent.y = y;
    ent.layer = layer;
    ent.startPos.set(x, y);
    ent.speed = new Vec2(sx, sy);
    ent.defSpeed = ent.speed.len();
    ent.defSize = size;
    ent.size = currSize(ent);
    ent.color = color.cpy();

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

  public void drawTrail(Particle c) {

  }

  public void init(Particle particle) {

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
