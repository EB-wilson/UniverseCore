package universecore.world.particles.models;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.util.Time;
import mindustry.graphics.Layer;
import universecore.world.particles.Particle;
import universecore.world.particles.ParticleModel;

public class ShapeParticle extends ParticleModel{
  public boolean circle = true;
  public int polySides = 4;
  public boolean outline;
  public float outlineStoke = 1.6f;
  public float rotSpeed;

  public float layer = Layer.effect;

  @Override
  public void draw(Particle p){
    float l = Draw.z();
    Draw.z(layer);

    Draw.color(p.color);
    if(circle){
      if(outline){
        Lines.stroke(outlineStoke);
        Lines.circle(p.x, p.y, p.size);
      }
      else Fill.circle(p.x, p.y, p.size);
    }
    else{
      if(outline){
        Lines.stroke(outlineStoke);
        Lines.poly(p.x, p.y, polySides, p.size, Time.time*rotSpeed);
      }
      else Lines.poly(p.x, p.y, polySides, p.size, Time.time*rotSpeed);
    }

    Draw.z(l);
  }
}
