package universeCore.world.consumers;

import mindustry.gen.Building;

import java.util.ArrayList;

public class UncConsumeType<T extends BaseConsume, R>{
  private static final ArrayList<UncConsumeType<?, ?>> allType = new ArrayList<>();
  private final int id;
  private final Class<T> type;
  private final Class<R> requireEntityType;
  
  public UncConsumeType(Class<T> type, Class<R> requireEntityType){
    id = allType.size();
    this.type = type;
    this.requireEntityType = requireEntityType;
    allType.add(this);
  }
  
  public Class<R> getRequire(){
    return requireEntityType;
  }
  
  public Class<T> getType(){
    return type;
  }
  
  public final int id(){
    return id;
  }
  
  public static UncConsumeType<?, ?>[] all(){
    return allType.toArray(new UncConsumeType[0]);
  }
  
  public static <Type extends BaseConsume, Require> UncConsumeType<Type, Require> add(Class<Type> type, Class<Require> requireEntityType){
    return new UncConsumeType<>(type, requireEntityType);
  }
  
  public static final UncConsumeType<UncConsumeItems, Building> item = new UncConsumeType<>(UncConsumeItems.class, Building.class);
  public static final UncConsumeType<UncConsumeLiquids, Building> liquid = new UncConsumeType<>(UncConsumeLiquids.class, Building.class);
  public static final UncConsumeType<UncConsumePower, Building> power = new UncConsumeType<>(UncConsumePower.class, Building.class);
}
