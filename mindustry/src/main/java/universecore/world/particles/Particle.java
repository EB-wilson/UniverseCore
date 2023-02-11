package universecore.world.particles;

import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Floatf;
import arc.func.Func;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.entities.EntityGroup;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.io.TypeIO;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import java.util.Iterator;
import java.util.LinkedList;

/**粒子的实体类，定义了可绘制的，可更新的实体对象
 * 可通过设置速度，位置，以及偏转方法改变粒子的运动轨迹，通常这个粒子具有数量上限，在正常情况下应当是性能安全的
 * 附带可控制拖尾
 * @author EBwilson */
@SuppressWarnings("unchecked")
public class Particle extends PosTeam implements Pool.Poolable, Drawc{
  private static int counter = 0;
  /**粒子的最大共存数量，总量大于此数目时，创建新的粒子会清除最先产生的粒子*/
  public static int maxAmount = 1024;
  
  protected static final LinkedList<Particle> all = new LinkedList<>();
  protected static final Seq<Particle> temp = new Seq<>();
  
  protected LinkedList<Cloud> tailing = new LinkedList<>();
  protected Queue<Cloud> freeQueue = new Queue<>();
  
  public Tile tile;
  
  protected Vec2 startPos = new Vec2();
  protected Vec2 tempPos = new Vec2();
  protected Seq<Cons<Particle>> deflects = new Seq<>();
  public Cloud currentCloud;
  
  /**粒子的速度，矢量*/
  public Vec2 speed = new Vec2();
  /**例子的初始速度大小，通常由计算生成，最好不要修改*/
  public float defSpeed;
  /**粒子的最大尺寸*/
  public float maxSize;
  /**粒子当前的尺寸，由计算获得，不要手动更改*/
  public float size;

  public Boolf<Particle> shouldCloud;
  public Func<Particle, Color> color;
  public Func<Particle, Color> tailColor;
  public Boolf<Particle> isFinal;
  public Floatf<Particle> particleSize;
  public Cons<Particle> update;
  public Cons<Particle> drawer;
  
  public Seq<Cons<Cloud>> cloudUpdaters = new Seq<>();

  static {
    EntityMapping.idMap[102] = Particle::new;
  }
  
  static Particle create(float x, float y, float sx, float sy, float size){
    if(counter >= maxAmount){
      all.getLast().remove();
    }
    Particle ent = Pools.obtain(Particle.class, Particle::new);
    ent.x = x;
    ent.y = y;
    ent.startPos.set(x, y);
    ent.speed = new Vec2(sx, sy);
    ent.defSpeed = ent.speed.len();
    ent.maxSize = size;
    ent.add();
    return ent;
  }
  
  public static Seq<Particle> get(Boolf<Particle> filter){
    temp.clear();
    for(Particle particle : all){
      if(filter.get(particle)) temp.add(particle);
    }
    return temp;
  }

  @Override
  public void draw(){
    Draw.z(Layer.effect);
    drawer.get(this);
    
    for(Cloud c: tailing){
      c.draw();
    }

    Draw.reset();
  }
  
  @Override
  public void read(Reads read){
    read.s();
    x = read.f();
    y = read.f();
    tile = TypeIO.readTile(read);
    
    this.afterRead();
  }
  
  @Override
  public void afterRead(){
  
  }
  
  @Override
  public void write(Writes writes){
    writes.s(1);
    writes.f(x);
    writes.f(y);
    TypeIO.writeTile(writes, tile);
  }
  
  @Override
  public boolean isAdded(){
    return added;
  }
  
  public void deflect(){
    for(Cons<Particle> deflect: deflects){
      deflect.get(this);
    }
  }
  
  @Override
  public void update(){
    deflect();
    x += speed.x*Time.delta;
    y += speed.y*Time.delta;

    if(shouldCloud != null){
      currentCloud = tailing.isEmpty() ? null : tailing.getLast();
      if(currentCloud == null){
        currentCloud = Pools.obtain(Cloud.class, Cloud::new);
        currentCloud.set(x, y, size, tailColor.get(this).cpy(), null);
        tailing.addLast(currentCloud);
      }

      currentCloud.x = x;
      currentCloud.y = y;
      currentCloud.size = size;

      if(shouldCloud.get(this)){
        Cloud cloud = Pools.obtain(Cloud.class, Cloud::new);
        cloud.set(x, y, size, tailColor.get(this).cpy(), currentCloud);
        tailing.addLast(cloud);
      }

      Iterator<Cloud> itr = tailing.iterator();

      while(itr.hasNext()){
        Cloud cld = itr.next();

        for(Cons<Cloud> updater: cloudUpdaters){
          updater.get(cld);
        }

        if(cld.size <= 0.1f){
          itr.remove();
          freeQueue.addLast(cld);
        }
      }
    }

    if(update != null) update.get(this);

    size = particleSize.get(this);
    if(isFinal.get(this) && tailing.size() == 0) remove();
  }
  
  @Override
  public void remove() {
    if (this.added) {
      all.remove(this);
      Groups.all.remove(this);
      Groups.draw.remove(this);
      Groups.queueFree(this);
      this.added = false;
      tailing.forEach(freeQueue::addLast);
      tailing.clear();
      while(!freeQueue.isEmpty()){
        Pools.free(freeQueue.removeFirst());
      }
      speed = new Vec2();
      counter--;
    }
  }
  
  @Override
  public void add(){
    if (!added) {
      all.addFirst(this);
      Groups.all.add(this);
      Groups.draw.add(this);
      added = true;
      counter++;
    }
  }
  
  @Override
  public boolean isLocal(){
    if(this instanceof Unitc u){
      return u.controller() != Vars.player;
    }
    
    return true;
  }
  
  @Override
  public boolean isRemote(){
    if (this instanceof Unitc u) {
      return u.isPlayer() && !this.isLocal();
    }
    return false;
  }
  
  @Override
  public boolean isNull(){
    return false;
  }
  
  @Override
  public <T extends Entityc> T self(){
    return (T)this;
  }
  
  @Override
  public <T> T as(){
    return (T)this;
  }
  
  @Override
  public int classId(){
    return 102;
  }
  
  @Override
  public boolean serialize(){
    return true;
  }
  
  @Override
  public int id(){
    return id;
  }
  
  @Override
  public void id(int id){
    this.id = id;
  }
  
  @Override
  public float clipSize(){
    return tempPos.set(x, y).sub(startPos).len();
  }
  
  @Override
  public void set(float x, float y){
    this.x = x;
    this.y = y;
  }
  
  @Override
  public void set(Position position){
    set(position.getX(), position.getY());
  }
  
  @Override
  public void trns(float x, float y) {
    set(this.x + x, this.y + y);
  }
  
  @Override
  public void trns(Position position){
    trns(position.getX(), position.getY());
  }
  
  @Override
  public int tileX() {
    return World.toTile(x);
  }
  
  @Override
  public int tileY() {
    return World.toTile(y);
  }
  
  @Override
  public Floor floorOn(){
    Tile tile = this.tileOn();
    return tile != null && tile.block() == Blocks.air ? tile.floor() : (Floor)Blocks.air;
  }

  @Override
  public Building buildOn(){
    return tileOn().build;
  }

  @Override
  public Block blockOn(){
    Tile tile = this.tileOn();
    return tile == null ? Blocks.air : tile.block();
  }
  
  @Override
  public boolean onSolid(){
    return false;
  }
  
  @Override
  public Tile tileOn(){
    return Vars.world.tileWorld(this.x, this.y);
  }
  
  @Override
  public float getX(){
    return x;
  }
  
  @Override
  public float getY(){
    return y;
  }
  
  @Override
  public float x(){
    return x;
  }
  
  @Override
  public void x(float x){
    this.x = x;
  }
  
  @Override
  public float y(){
    return y;
  }
  
  @Override
  public void y(float y){
    this.y = y;
  }
  
  @Override
  public void reset(){
    speed.setZero();
    id = EntityGroup.nextId();
    x = 0;
    y = 0;
    deflects.clear();
    startPos.setZero();
    tempPos.setZero();
    for(Cloud cloud: tailing){
      Pools.free(cloud);
    }
    tailing.clear();
    for(Cloud cloud: freeQueue){
      Pools.free(cloud);
    }
    freeQueue.clear();
    defSpeed = 0;
    maxSize = 0;
    size = 0;

    color = null;
    tailColor = null;
    isFinal = null;
    particleSize = null;
    update = null;
    drawer = null;

    cloudUpdaters.clear();

    added = false;
  }

  public class Cloud implements Pool.Poolable{
    private final Vec2 vec = new Vec2();

    public Color color;
    
    public float x, y, size;
    public Cloud lastCloud;
    
    public void set(float x, float y, float size, Color color, Cloud last){
      this.x = x;
      this.y = y;
      this.size = size;
      this.color = color;

      if(last != null){
        lastCloud = last;
      }
      else{
        lastCloud = Pools.obtain(StaticCloud.class, StaticCloud::new);
        lastCloud.x = x;
        lastCloud.y = y;
        lastCloud.color = color;
      }
    }
    
    public Vec2 vector(){
      return vec.set(x, y).sub(lastCloud.x, lastCloud.y);
    }
    
    public void draw(){
      Draw.color(color);
      float rad = Tmp.v1.set(x, y).sub(lastCloud.x, lastCloud.y).angle();
      Tmp.v1.set(0, lastCloud.size/2).rotate(rad);
      Tmp.v2.set(0, size/2).rotate(rad);

      Fill.quad(lastCloud.x + Tmp.v1.x, lastCloud.y + Tmp.v1.y,
          lastCloud.x - Tmp.v1.x, lastCloud.y - Tmp.v1.y,
          x - Tmp.v2.x, y - Tmp.v2.y,
          x + Tmp.v2.x, y + Tmp.v2.y);
    }

    @Override
    public void reset(){
      x = 0;
      y = 0;
      size = 0;
      color = null;
      if(lastCloud instanceof StaticCloud){
        Pools.free(lastCloud);
      }
      lastCloud = null;
    }
  }

  private static final Vec2 zero = new Vec2();

  private class StaticCloud extends Cloud{
    @Override
    public Vec2 vector(){
      return zero;
    }
  }
}
