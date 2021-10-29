package universeCore.world.consumers;

import mindustry.gen.Building;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class UncConsumeType<T extends BaseConsume<?>>{
  private static final ArrayList<UncConsumeType<?>> allType = new ArrayList<>();
  private final int id;
  private final Class<T> type;
  
  public UncConsumeType(Class<T> type){
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
  
  public static UncConsumeType<?>[] all(){
    return allType.toArray(new UncConsumeType[0]);
  }
  
  public static <Type extends BaseConsume<?>> UncConsumeType<? extends Type> add(Class<Type> type){
    return new UncConsumeType<>(type);
  }
  
  public static final UncConsumeType<UncConsumeItems<?>> item = (UncConsumeType<UncConsumeItems<?>>) add(UncConsumeItems.class);
  public static final UncConsumeType<UncConsumeLiquids<?>> liquid = (UncConsumeType<UncConsumeLiquids<?>>) add(UncConsumeLiquids.class);
  public static final UncConsumeType<UncConsumePower<?>> power = (UncConsumeType<UncConsumePower<?>>) add(UncConsumePower.class);
}
