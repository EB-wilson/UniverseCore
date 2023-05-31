package universecore.world.particles;

import arc.graphics.Color;
import arc.math.geom.Vec2;
import arc.util.pooling.Pools;
import mindustry.graphics.Layer;

public class ParticleModel{
  /**@deprecated 此特性与模型模式设计初衷相悖，已废弃*/
  @Deprecated
  public Color color = Color.white;
  /**@deprecated 此特性与模型模式设计初衷相悖，已废弃*/
  @Deprecated
  public Color trailColor = Color.white;

  /**@deprecated 应当使用传递颜色作为元数据的方法，而非由模型进行默认设置*/
  @Deprecated
  public Particle create(float x, float y, float sx, float sy, float size){
    return create(x, y, color, sx, sy, size, Layer.effect);
  }

  /**@deprecated 应当使用传递颜色作为元数据的方法，而非由模型进行默认设置*/
  @Deprecated
  public Particle create(float x, float y, float sx, float sy, float size, float layer) {
    return create(x, y, color, sx, sy, size, layer);
  }

  /**使用该模型创建一个粒子的实例
   *
   * @param x 粒子创建时的x坐标
   * @param y 粒子创建时的y坐标
   * @param color 粒子的颜色
   * @param sx 粒子初始运动速度的x分量
   * @param sy 粒子初始运动速度的y分量
   * @param size 粒子的尺寸*/
  public Particle create(float x, float y, Color color, float sx, float sy, float size){
    return create(x, y, color, sx, sy, size, Layer.effect);
  }

  /**使用该模型创建一个粒子的实例
   *
   * @param x 粒子创建时的x坐标
   * @param y 粒子创建时的y坐标
   * @param color 粒子的颜色
   * @param sx 粒子初始运动速度的x分量
   * @param sy 粒子初始运动速度的y分量
   * @param size 粒子的尺寸
   * @param layer 粒子所在的层，这只在绘制流程中使用*/
  public Particle create(float x, float y, Color color, float sx, float sy, float size, float layer){
    Particle ent = Pools.obtain(Particle.class, Particle::new);
    ent.x = x;
    ent.y = y;
    ent.color.set(color);
    ent.layer = layer;
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

  public void drawTrail(Particle c) {

  }

  public void init(Particle particle) {

  }

  public boolean isFinal(Particle p){
    return false;
  }

  public Color trailColor(Particle p){
    return null;
  }

  public float currSize(Particle p){
    return p.defSize;
  }

  public boolean isFaded(Particle p, Particle.Cloud cloud){
    return false;
  }

}
