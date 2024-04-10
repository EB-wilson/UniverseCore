package universecore.world.particles;

import arc.graphics.Color;
import arc.util.pooling.Pools;
import mindustry.graphics.Layer;

public class ParticleModel{
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
   * @param parent 粒子所属的父级粒子
   * @param x 粒子创建时的x坐标
   * @param y 粒子创建时的y坐标
   * @param color 粒子的颜色
   * @param sx 粒子初始运动速度的x分量
   * @param sy 粒子初始运动速度的y分量
   * @param size 粒子的尺寸*/
  public Particle create(Particle parent, float x, float y, Color color, float sx, float sy, float size){
    return create(parent, x, y, color, sx, sy, size, Layer.effect);
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
    return create(null, x, y, color, sx, sy, size, layer);
  }

  /**使用该模型创建一个粒子的实例
   *
   * @param parent 粒子所属的父级粒子
   * @param x 粒子创建时的x坐标
   * @param y 粒子创建时的y坐标
   * @param color 粒子的颜色
   * @param sx 粒子初始运动速度的x分量
   * @param sy 粒子初始运动速度的y分量
   * @param size 粒子的尺寸
   * @param layer 粒子所在的层，这只在绘制流程中使用*/
  public Particle create(Particle parent, float x, float y, Color color, float sx, float sy, float size, float layer){
    Particle ent = Pools.obtain(Particle.class, Particle::new);
    ent.parent = parent;
    ent.x = x;
    ent.y = y;
    ent.color.set(color);
    ent.layer = layer;
    ent.startPos.set(x, y);
    ent.speed.set(sx, sy);
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
