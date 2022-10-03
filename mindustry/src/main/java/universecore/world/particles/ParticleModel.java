package universecore.world.particles;

import arc.func.*;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;

public class ParticleModel{
  /**粒子的颜色*/
  public Func<Particle, Color> color;
  /**拖尾的颜色*/
  public Func<Particle, Color> tailColor;
  /**判断粒子是已结束*/
  public Boolf<Particle> isFinal;
  /**粒子的尺寸计算公式*/
  public Floatf<Particle> particleSize;
  /**粒子更新触发器，每刻调用一次*/
  public Cons<Particle> update;
  /**粒子绘制器，draw时调用*/
  public Cons<Particle> drawer;
  /**拖尾更新器，每刻执行，依次传入每一个云对象*/
  public Seq<Cons<Particle.Cloud>> cloudUpdaters = new Seq<>();
  /**拖尾的创建判据，这将回调此粒子此时是否应当创建拖尾*/
  public Boolf<Particle> shouldCloud;
  
  public Seq<Cons<Particle>> deflects = new Seq<>();
  
  public Particle create(float x, float y, float sx, float sy, float size){
    Particle inst = Particle.create(x, y, sx, sy, size);

    inst.shouldCloud = shouldCloud;

    if(color != null) inst.color = color;
    if(tailColor != null){
      inst.tailColor = tailColor;
    }
    else inst.tailColor = color;

    if(isFinal != null) inst.isFinal = isFinal;
    if(particleSize != null) inst.particleSize = particleSize;
    if(update != null) inst.update = update;
    if(drawer != null) inst.drawer = drawer;

    inst.cloudUpdaters.add(cloudUpdaters);
    inst.deflects.add(deflects);
    
    return inst;
  }

  public ParticleModel setDefault(){
    this.color = e -> Color.white;
    this.particleSize = e -> e.maxSize*(e.speed.len()/e.defSpeed);
    this.isFinal = e -> e.speed.len() <= 0.25f;
    this.drawer = e -> {
      Draw.color(color.get(e));
      Fill.circle(e.x, e.y, e.size/2);
      Draw.reset();
    };
    this.setTailFade(e -> e -= 0.075f*Time.delta);
    this.setCloudThreshold(2, 24);

    return this;
  }

  public ParticleModel setColor(Func<Particle, Color> color){
    this.color = color;
    return this;
  }

  public ParticleModel setColor(Color color){
    return setColor(e -> color);
  }

  public ParticleModel setTailColor(Func<Particle, Color> color){
    this.tailColor = color;
    return this;
  }

  public ParticleModel setTailColor(Color color){
    return setTailColor(e -> color);
  }

  public ParticleModel resetDeflect(){
    deflects.clear();
    return this;
  }

  public ParticleModel resetCloudUpdate(){
    cloudUpdaters.clear();
    return this;
  }

  /**设置粒子的移动目的地，设置后粒子的朝向无论运动如何，运动方向都会向目的地靠近*/
  @SuppressWarnings("all")
  public ParticleModel setDest(Vec2 destPos, float deflection){
    return setDest(destPos.x, destPos.y, deflection);
  }

  /**设置粒子的移动目的地，设置后粒子的朝向无论运动如何，运动方向都会向目的地靠近
   *
   * @param destX 目标点x坐标
   * @param destY 目标点y坐标
   * @param deflection 偏转系数，数值越大则趋近越快*/
  @SuppressWarnings("all")
  public ParticleModel setDest(float destX, float destY, float deflection){
    deflects.add(e -> {
      float from = e.speed.angle();
      float to = Tmp.v1.set(destX, destY).sub(e.x, e.y).angle();
      float r = to - from;
      r = r > 180? r-360: r < -180? r+360: r;
      e.speed.rotate(r*deflection*Time.delta);
    });
    isFinal = e -> Mathf.len(e.x - destX, e.y - destY) <= 4f;
    return this;
  }

  /**设置粒子的随机粒子运动速度偏转行为
   *
   * @param deflectAngle 粒子随机偏转的随机向量角度范围，取此数值的正负构成区间，以粒子速度的反方向为零角
   * @param strength 粒子运动时所受阻力大小，这会影响粒子在平移时的随机偏转强度，若为0粒子不会偏转和自己停下来*/
  public ParticleModel setDeflect(float deflectAngle, float strength){
    deflects.add(e -> {
      float angle = Tmp.v1.set(e.speed).scl(-1).angle();
      float scl = Math.max(e.speed.len()/e.defSpeed, 0.1f)*Time.delta*strength;
      Tmp.v2.set(e.speed).setAngle(angle + Mathf.random(-deflectAngle, deflectAngle)).scl(scl);
      e.speed.add(Tmp.v2);
    });
    return this;
  }

  /**设置粒子的拖尾分节阈值，当前一道拖尾与当前拖尾的角度偏移达到这个数值时才会产生下一条轨迹，数值越小，轨迹越平滑，但性能开销越大
   *
   * @param angleThreshold 粒子轨迹拖尾的转折阈值*/
  public ParticleModel setCloudThreshold(float angleThreshold, float distanceThreshold){
    shouldCloud = e -> {
      Particle.Cloud cl = e.currentCloud;
      return Math.abs(cl.vector().angle() - cl.lastCloud.vector().angle()) > angleThreshold*Time.delta
      || Mathf.len(cl.x - cl.lastCloud.x, cl.y - cl.lastCloud.y) > distanceThreshold;
    };
    return this;
  }

  /**使拖尾颜色逐一向目标颜色趋近
   *
   * @param targetColor 目标颜色
   * @param lerp 趋近速度*/
  public ParticleModel tailGradient(Color targetColor, float lerp){
    cloudUpdaters.add(e -> e.color.lerp(targetColor, lerp*Time.delta));
    return this;
  }

  /**设置拖尾的衰减算法，以拖尾的尺寸为传入参数进行回调，计算结果用以为拖尾重新设置尺寸，当某一段拖尾的尺寸小于0.1时其会被移除
   *
   * @param fader 拖尾消退的迭代式*/
  public ParticleModel setTailFade(FloatFloatf fader){
    cloudUpdaters.add(e -> e.size = fader.get(e.size));
    return this;
  }

  public ParticleModel setRegion(TextureRegion region, boolean rotate){
    drawer = rotate? e -> Draw.rect(region, e.x, e.y, e.speed.angle()): e -> Draw.rect(region, e.x, e.y);
    return this;
  }

  public ParticleModel setDraw(Cons<Particle> drawer){
    this.drawer = drawer;
    return this;
  }
}
