package universecore.world.particles;

import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.geom.Vec2;
import arc.struct.OrderedSet;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.gen.Decal;
import mindustry.graphics.Layer;
import universecore.annotations.Annotations;
import universecore.components.ExtraVariableComp;

import java.util.Iterator;

/**粒子的实体类，定义了可绘制的，可更新的实体对象
 * 可通过设置速度，位置，以及偏转方法改变粒子的运动轨迹，通常这个粒子具有数量上限，在正常情况下应当是性能安全的
 * 附带可控制拖尾
 * @author EBwilson */
@Annotations.ImplEntries
public class Particle extends Decal implements ExtraVariableComp, Iterable<Particle.Cloud>{
  private static int counter = 0;
  /**粒子的最大共存数量，总量大于此数目时，创建新的粒子会清除最先产生的粒子*/
  public static int maxAmount = 1024;
  
  protected static final OrderedSet<Particle> all = new OrderedSet<>();
  protected static final Seq<Particle> temp = new Seq<>();
  
  protected Vec2 startPos = new Vec2();
  protected float clipSize;

  Cloud currentCloud, firstCloud;
  int cloudCount;
  
  /**粒子的速度，矢量*/
  public Vec2 speed = new Vec2();
  /**粒子当前的尺寸*/
  public float size;

  public float defSpeed;
  public float defSize;

  /**粒子模型，决定了该粒子的行为*/
  public ParticleModel model;
  public float layer;

  public static Seq<Particle> get(Boolf<Particle> filter){
    temp.clear();
    for(Particle particle : all){
      if(filter.get(particle)) temp.add(particle);
    }
    return temp;
  }

  @Override
  public void add(){
    super.add();
    counter++;

    currentCloud = Pools.obtain(Cloud.class, Cloud::new);
    currentCloud.x = x;
    currentCloud.y = y;
    currentCloud.size = 0;
    currentCloud.color = model.trailColor(this).cpy();

    firstCloud = currentCloud;

    if(counter >= maxAmount && !all.isEmpty()){
      all.orderedItems().get(all.size - 1).remove();
    }
  }

  @Override
  public void draw(){
    float l = Draw.z();
    Draw.z(layer);
    model.draw(this);

    if(currentCloud != null){
      model.drawTrail(this);
    }

    Draw.z(l);
    Draw.reset();
  }
  
  @Override
  public void update(){
    model.deflect(this);

    x += speed.x*Time.delta;
    y += speed.y*Time.delta;

    size = model.currSize(this);

    Cloud c = Pools.obtain(Cloud.class, Cloud::new);
    c.x = x;
    c.y = y;
    c.size = size;
    c.color = model.trailColor(this).cpy();

    c.perCloud = currentCloud;
    currentCloud.nextCloud = c;

    currentCloud = c;

    cloudCount++;

    for(Cloud cloud: currentCloud){
      model.updateTrail(this, cloud);
    }

    boolean mark = false;
    while(firstCloud.nextCloud != null){
      if(model.isFaded(this, firstCloud)){
        Cloud n = firstCloud.nextCloud;
        n.perCloud = null;
        Pools.free(firstCloud);
        firstCloud = n;

        mark = true;
        cloudCount--;
      }
      else break;
    }

    if(!mark && model.isFinal(this)){
      Cloud n = firstCloud.nextCloud;
      n.perCloud = null;
      Pools.free(firstCloud);
      firstCloud = n;

      cloudCount--;
    }

    if(cloudCount <= 4 && model.isFinal(this)) remove();
  }

  @Override
  public void remove() {
    if (this.added) {
      all.remove(this);
      counter--;
    }
    super.remove();
  }

  @Override
  public int classId(){
    return 102;
  }

  @Override
  public float clipSize(){
    return clipSize = Math.max(Tmp.v1.set(x, y).sub(startPos).len(), clipSize);
  }

  @Override
  public void reset(){
    super.reset();

    speed.setZero();
    startPos.setZero();

    layer = 0;
    clipSize = 0;

    while(firstCloud.nextCloud != null){
      Cloud n = firstCloud.nextCloud;
      n.perCloud = null;
      Pools.free(firstCloud);
      firstCloud = n;
    }
    Pools.free(firstCloud);

    currentCloud = null;
    firstCloud = null;

    cloudCount = 0;
    size = 0;
    extra().clear();

    model = null;

    color = null;
  }

  @Override
  public Iterator<Cloud> iterator() {
    return currentCloud.iterator();
  }

  public static class Cloud implements Pool.Poolable, Iterable<Cloud>{
    public Color color;
    
    public float x, y, size;
    public Cloud perCloud, nextCloud;
    
    public void draw(){
      Draw.color(color);

      if(perCloud != null && nextCloud != null){
        float angle = Angles.angle(x - perCloud.x, y - perCloud.y);
        float dx1 = Angles.trnsx(angle + 90, size);
        float dy1 = Angles.trnsy(angle + 90, size);
        angle = Angles.angle(nextCloud.x - x, nextCloud.y - y);
        float dx2 = Angles.trnsx(angle + 90, nextCloud.size);
        float dy2 = Angles.trnsy(angle + 90, nextCloud.size);

        Fill.quad(
            x + dx1, y + dy1,
            x - dx1, y - dy1,
            nextCloud.x - dx2, nextCloud.y - dy2,
            nextCloud.x + dx2, nextCloud.y + dy2
        );
      }
      else if(perCloud == null && nextCloud != null){
        float angle = Angles.angle(nextCloud.x - x, nextCloud.y - y);
        float dx2 = Angles.trnsx(angle + 90, nextCloud.size);
        float dy2 = Angles.trnsy(angle + 90, nextCloud.size);

        Fill.quad(
            x, y,
            x, y,
            nextCloud.x - dx2, nextCloud.y - dy2,
            nextCloud.x + dx2, nextCloud.y + dy2
        );
      }
    }

    @Override
    public void reset(){
      x = 0;
      y = 0;
      size = 0;
      color = null;

      perCloud = null;
      nextCloud = null;
    }

    @Override
    public Iterator<Cloud> iterator(){
      return new Iterator<>(){
        Cloud curr = Cloud.this;

        @Override
        public boolean hasNext(){
          return curr.perCloud != null;
        }

        @Override
        public Cloud next(){
          return curr = curr.perCloud;
        }
      };
    }
  }
}
