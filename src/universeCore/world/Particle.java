package universeCore.world;

import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pool;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.entities.EntityGroup;
import mindustry.gen.Drawc;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import java.util.LinkedList;

public class Particle implements Pool.Poolable, Drawc{
  protected static final LinkedList<Particle> all = new LinkedList<>();
  protected static final Seq<Particle> temp = new Seq<>();
  
  protected Seq<Cloud> tailing = new Seq<>();
  protected Cloud currentCloud;
  
  public Tile tile;
  
  public Vec2 speed = new Vec2();
  public float defSpeed;
  public float maxSize;
  public float size;
  
  public float attenuate = 0.2f;
  public float angleThreshold = 1f;
  public float deflectAngle = 90f;
  
  public Color color = Pal.reactorPurple;
  
  public transient int id = EntityGroup.nextId();
  public boolean added = false;
  public float x, y;
  
  public Cons<Particle> deflect;
  public Cons<Particle> headRegion = e -> {
    Fill.circle(e.x, e.y, e.size);
  };
  public Cons<Cloud> cloudUpdater = e -> {
     e.size = Mathf.lerpDelta(e.size, 0, 0.05f);
  };
  
  protected Vec2 startPos = new Vec2();
  protected Vec2 tempPos = new Vec2();
  
  public static Particle create(float x, float y, float sx, float sy){
    return create(x, y, sx, sy, 5);
  }
  
  public static Particle create(float x, float y, float sx, float sy, float size){
    Particle ent = new Particle();
    ent.x = x;
    ent.y = y;
    ent.startPos.set(x, y);
    ent.speed = new Vec2(sx, sy);
    ent.defSpeed = ent.speed.len();
    ent.maxSize = size;
    ent.add();
    return ent;
  }
  
  public static Seq<Particle> get(Boolf<Particle> valid){
    temp.clear();
    for(Particle particle : all){
      if(valid.get(particle)) temp.add(particle);
    }
    return temp;
  }
  
  @Override
  public void draw(){
    Draw.z(Layer.effect);
    for(Cloud c: tailing){
      c.draw();
    }
    Draw.color(Pal.accent);
    
    Draw.reset();
  }
  
  @Override
  public void read(Reads read){
    short REV = read.s();
    if (REV == 0) {
      x = read.f();
      read.i();
    } else {
      if (REV != 1) throw new IllegalArgumentException("Unknown revision '" + REV + "' for entity type 'PuddleComp'");
      x = read.f();
    }
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
  
  public Particle setDest(float x, float y){
    return setDest(x, y, 0.035f, false);
  }
  
  @SuppressWarnings("all")
  public Particle setDest(float x, float y, float deflection, boolean linkLast){
    Vec2 dest = new Vec2(x, y);
    Cons<Particle>[] last = new Cons[1];
    if(linkLast && deflect != null) last[0] = deflect;
    deflect = e -> {
      if(last[0] != null) last[0].get(e);
      float from = e.speed.angle();
      float to = Tmp.v1.set(dest.x, dest.y).sub(e.x, e.y).angle();
      float r = to - from;
      r = r > 180? r-360: r < -180? r+360: r;
      e.speed.rotate(r*deflection*Time.delta);
    };
    return this;
  }
  
  public Particle setAttenuate(float att){
    attenuate = att;
    return this;
  }
  
  public void deflection(){
    float angle = Tmp.v1.set(speed).scl(-1).angle();
    Tmp.v2.set(speed).setAngle(angle + Mathf.random(-deflectAngle, deflectAngle)).scl(speed.len()/defSpeed*attenuate*Time.delta);
    speed.add(Tmp.v2);
    
    if(deflect != null) deflect.get(this);
  }
  
  @Override
  public void update(){
    deflection();
    x += speed.x*Time.delta;
    y += speed.y*Time.delta;
    
    float speedRate = speed.len();
    float rate = speedRate/defSpeed;
    
    if(currentCloud == null){
      currentCloud = new Cloud(x, y, size, color);
      tailing.add(currentCloud);
    }
    
    if(speedRate/defSpeed > 0.05f){
      currentCloud.x = x;
      currentCloud.y = y;
    
      Vec2 currVec = currentCloud.vector();
      Vec2 lastVec = currentCloud.lastCloud.vector().setLength(currVec.len());
      if((Math.abs(currVec.x - lastVec.y) > angleThreshold || Math.abs(currVec.y - lastVec.y) > angleThreshold)){
        Cloud cloud = new Cloud(x, y, size, color);
        cloud.lastCloud = currentCloud;
        tailing.add(cloud);
        currentCloud = cloud;
      }
    }
    
    for(Cloud c: tailing){
      if(c == null) continue;
      c.update();
      if(c.size <= 0.05f) tailing.remove(c);
    }
    
    size = rate*maxSize;
    if(rate <= 0.05f && tailing.size == 0) remove();
  }
  
  @Override
  public void remove() {
    if (this.added) {
      all.remove(this);
      Groups.all.remove(this);
      Groups.draw.remove(this);
      this.added = false;
      tailing = null;
      speed = new Vec2();
    }
  }
  
  @Override
  public void add(){
    if (!added) {
      all.add(this);
      Groups.all.add(this);
      Groups.draw.add(this);
      added = true;
    }
  }
  
  @Override
  public boolean isLocal(){
    if(this instanceof Unitc){
      Unitc u = (Unitc) this;
      return u.controller() != Vars.player;
    }
    
    return true;
  }
  
  @Override
  public boolean isRemote(){
    if (this instanceof Unitc) {
      Unitc u = (Unitc)this;
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
    speed = new Vec2();
    id = EntityGroup.nextId();
    x = 0;
    y = 0;
    added = false;
  }
  
  public class Cloud{
    public Cloud lastCloud;
    
    public float size;
    public Color color;
    
    public float x, y;
    
    public Cloud(float x, float y, float size, Color color){
      lastCloud = new Cloud(x, y, color){
        @Override
        public Vec2 vector(){
          return new Vec2(0, 0);
        }
      };
      this.x = x;
      this.y = y;
      this.size = size;
      this.color = color;
    }
    
    private Cloud(float x, float y, Color color){
      this.x = x;
      this.y = y;
      this.color = color;
    }
    
    public Vec2 vector(){
      return new Vec2(x, y).sub(lastCloud.x, lastCloud.y);
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
    
    public void update(){
      cloudUpdater.get(this);
    }
  }
}
