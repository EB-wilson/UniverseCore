package universecore.ui.elements;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mat;
import arc.math.Rand;
import arc.scene.Element;
import arc.scene.Group;
import arc.util.Align;
import arc.util.Time;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.entities.Effect;

public class SceneEffect extends Element implements Pool.Poolable {
  private static final Rand idRand = new Rand();

  private final Effect.EffectContainer container = new Effect.EffectContainer();
  private final Mat transform = new Mat(), last = new Mat();
  private final Color tmpColor = new Color();

  public int id;
  public Effect effect;
  public float lifetime;
  public float time;
  public Object data;

  public static SceneEffect showOnStage(Effect fx, float x, float y){
    return showOnStage(fx, x, y, 0, Color.white, null);
  }
  public static SceneEffect showOnStage(Effect fx, float x, float y, float rotation){
    return showOnStage(fx, x, y, rotation, Color.white, null);
  }
  public static SceneEffect showOnStage(Effect fx, float x, float y, float rotation, Color color){
    return showOnStage(fx, x, y, rotation, color, null);
  }
  public static SceneEffect showOnStage(Effect fx, float x, float y, float rotation, Object data){
    return showOnStage(fx, x, y, rotation, Color.white, data);
  }
  public static SceneEffect showOnStage(Effect fx, float x, float y, float rotation, Color color, Object data){
    SceneEffect e = Pools.obtain(SceneEffect.class, SceneEffect::new);
    Core.scene.add(e);

    return setDefAttr(fx, x, y, rotation, color, data, e);
  }

  public static SceneEffect showOnGroup(Group target, Effect fx, float x, float y){
    return showOnGroup(target, fx, x, y, 0, Color.white, null);
  }
  public static SceneEffect showOnGroup(Group target, Effect fx, float x, float y, float rotation){
    return showOnGroup(target, fx, x, y, rotation, Color.white, null);
  }
  public static SceneEffect showOnGroup(Group target, Effect fx, float x, float y, float rotation, Color color){
    return showOnGroup(target, fx, x, y, rotation, color, null);
  }
  public static SceneEffect showOnGroup(Group target, Effect fx, float x, float y, float rotation, Object data){
    return showOnGroup(target, fx, x, y, rotation, Color.white, data);
  }
  public static SceneEffect showOnGroup(Group target, Effect fx, float x, float y, float rotation, Color color, Object data){
    SceneEffect e = Pools.obtain(SceneEffect.class, SceneEffect::new);
    target.addChild(e);

    return setDefAttr(fx, x, y, rotation, color, data, e);
  }

  private static SceneEffect setDefAttr(Effect fx, float x, float y, float rotation, Color color, Object data, SceneEffect e) {
    idRand.setSeed(System.nanoTime());
    e.id = idRand.nextInt();
    e.effect = fx;
    e.lifetime = fx.lifetime;
    e.data = data;
    e.time = 0f;
    e.setPosition(x, y, Align.center);
    e.setRotation(rotation);
    e.color.set(color);

    return e;
  }

  private SceneEffect(){}

  @Override
  public void act(float delta) {
    super.act(delta);

    time += Time.delta;
    if (time > lifetime){
      remove();
    }
  }

  @Override
  public void draw() {
    super.draw();
    transform.set(last.set(Draw.trans()));
    transform.translate(x + width/2, y + height/2);
    transform.scale(scaleX, scaleY);

    Draw.reset();
    Draw.trans(transform);
    container.set(id, tmpColor.set(color).a(color.a*parentAlpha), time, lifetime, rotation, 0, 0, data);
    effect.render(container);
    Draw.reset();
    Draw.trans(last);
  }

  @Override
  public boolean remove() {
    boolean b = super.remove();
    if (b) Pools.free(this);
    return b;
  }

  @Override
  public void reset() {
    effect = null;
    lifetime = 0;
    time = 0;
    data = null;
    width = 0;
    height = 0;
    scaleX = 1;
    scaleY = 1;
    color.set(Color.white);
    x = 0;
    y = 0;
    rotation = 0;
    id = 0;
  }
}
