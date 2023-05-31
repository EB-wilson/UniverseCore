import dynamilize.DynamicClass;
import dynamilize.DynamicMaker;

public class HotUpdateDemo{
  public static final DynamicClass Updater = DynamicClass.get("Updater");

  static {
    Updater.setFunction("update", (s, sup, a) -> {
      sup.invokeFunc("update", a);
    });
  }

  public static void main(String[] args){
    World world = new World();
    while(true){//主循环
      world.update();
    }
  }
}

class World implements Module{
  DynamicMaker maker = DynamicMaker.getDefault();

  public Scene scene = new Scene();
  public Entities entities = maker.newInstance(Entities.class, HotUpdateDemo.Updater).objSelf();

  @Override
  public void update(){
    scene.update();
    entities.update();
  }
}

class Scene implements Module{
  @Override
  public void update(){
    //draw scene
  }
}

class Entities implements Module{
  @Override
  public void update(){
    //update status of entity
  }
}

interface Module{
  void update();
}
