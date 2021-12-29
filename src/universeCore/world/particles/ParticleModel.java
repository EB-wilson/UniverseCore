package universeCore.world.particles;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Floatf;
import arc.func.Func;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;

import java.awt.*;

public class ParticleModel{
  /**粒子的颜色*/
  public Func<Particle, arc.graphics.Color> color;
  /**拖尾的颜色*/
  public Func<Particle, arc.graphics.Color> tailColor;
  /**判断粒子是已结束*/
  public Boolf<Particle> isFinal;
  /**粒子的尺寸计算公式*/
  public Floatf<Particle> sizeF;
  /**粒子更新触发器，每刻调用一次*/
  public Cons<Particle> update;
  /**粒子绘制器，draw时调用*/
  public Cons<Particle> regionDraw;
  /**拖尾更新器，每刻执行，依次传入每一个云对象*/
  public Cons<Particle.Cloud> cloudUpdater;
  
  /**粒子运动时所受阻力大小，这会影响粒子在平移时的随机偏转强度，为0时粒子不会自己停下来*/
  public float attenuate = 0.2f;
  /**粒子轨迹拖尾的转折阈值，当前一道拖尾与当前拖尾的角度偏移达到这个数值时才会产生下一条轨迹，数值越小，轨迹越平滑，但性能开销越大*/
  public float angleThreshold = 3f;
  /**粒子随机偏转的随机向量角度范围，取此数值的正负构成区间，以例子速度的反方向为零角*/
  public float deflectAngle = 90f;
  
  public Particle create(float x, float y, float sx, float sy, float size){
    Particle inst = Particle.create(x, y, sx, sy, size);
    inst.attenuate = attenuate;
    inst.angleThreshold = angleThreshold;
    inst.deflectAngle = deflectAngle;
    
    if(color != null) inst.color = color;
    if(tailColor != null) inst.tailColor = tailColor;
    if(isFinal != null) inst.isFinal = isFinal;
    if(sizeF != null) inst.isFinal = isFinal;
    if(update != null) inst.update = update;
    if(regionDraw != null) inst.regionDraw = regionDraw;
    if(cloudUpdater != null) inst.cloudUpdater = cloudUpdater;
    
    return inst;
  }
}
