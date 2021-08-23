package universeCore.world.producers;

import mindustry.gen.Building;

import java.util.ArrayList;

public class ProduceType<T extends BaseProduce, R>{
  private static final ArrayList<ProduceType<?, ?>> allType = new ArrayList<>();
  
  private final int id;
  private final Class<T> type;
  private final Class<R> requireEntityType;
  
  public ProduceType(Class<T> type, Class<R> requireEntityType){
    id = allType.size();
    this.type = type;
    this.requireEntityType = requireEntityType;
    allType.add(this);
  }
  
  public Class<T> getType(){
    return type;
  }
  
  public Class<R> getRequire(){
    return requireEntityType;
  }
  
  public final int id(){
    return id;
  }
  
  public static ProduceType<?, ?>[] all(){
    return allType.toArray(new ProduceType[0]);
  }
  
  public static <Type extends BaseProduce, Require> ProduceType<Type, Require> add(Class<Type> type, Class<Require> requireType){
    return new ProduceType<>(type, requireType);
  }
  
  public static final ProduceType<ProduceItems, Building> item = new ProduceType<>(ProduceItems.class, Building.class);
  public static final ProduceType<ProduceLiquids, Building> liquid = new ProduceType<>(ProduceLiquids.class, Building.class);
  public static final ProduceType<ProducePower, Building> power = new ProduceType<>(ProducePower.class, Building.class);
}
