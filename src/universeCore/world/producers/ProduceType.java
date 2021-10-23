package universeCore.world.producers;

import mindustry.gen.Building;
import rhino.module.Require;

import java.util.ArrayList;

public class ProduceType<T extends BaseProduce<?>>{
  private static final ArrayList<ProduceType<?>> allType = new ArrayList<>();
  
  private final int id;
  private final Class<T> type;
  
  public ProduceType(Class<T> type){
    id = allType.size();
    this.type = type;
    allType.add(this);
  }
  
  public Class<T> getType(){
    return type;
  }
  
  public final int id(){
    return id;
  }
  
  public static ProduceType<?>[] all(){
    return allType.toArray(new ProduceType[0]);
  }
  
  public static <Type extends BaseProduce<?>> ProduceType<Type> add(Class<Type> type){
    return new ProduceType<>(type);
  }
  
  public static final ProduceType<ProduceItems> item = add(ProduceItems.class);
  public static final ProduceType<ProduceLiquids> liquid = add(ProduceLiquids.class);
  public static final ProduceType<ProducePower> power = add(ProducePower.class);
}
