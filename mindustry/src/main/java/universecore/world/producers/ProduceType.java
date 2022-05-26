package universecore.world.producers;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
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
  
  public static <Type extends BaseProduce<?>> ProduceType<? extends Type> add(Class<Type> type){
    return new ProduceType<>(type);
  }
  
  public static final ProduceType<ProduceItems<?>> item = (ProduceType<ProduceItems<?>>) add(ProduceItems.class);
  public static final ProduceType<ProduceLiquids<?>> liquid = (ProduceType<ProduceLiquids<?>>) add(ProduceLiquids.class);
  public static final ProduceType<ProducePower<?>> power = (ProduceType<ProducePower<?>>) add(ProducePower.class);
}
