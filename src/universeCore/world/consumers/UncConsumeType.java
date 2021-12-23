package universeCore.world.consumers;

import mindustry.ctype.ContentType;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class UncConsumeType<T extends BaseConsume<?>>{
  private static final ArrayList<UncConsumeType<?>> allType = new ArrayList<>();
  private final int id;
  private final Class<T> type;
  private final ContentType contType;
  
  public UncConsumeType(Class<T> type, ContentType cType){
    id = allType.size();
    this.type = type;
    contType = cType;
    allType.add(this);
  }
  
  public Class<T> getType(){
    return type;
  }
  
  public final ContentType cType(){
    return contType;
  }
  
  public final int id(){
    return id;
  }
  
  public static UncConsumeType<?>[] all(){
    return allType.toArray(new UncConsumeType[0]);
  }
  
  public static <Type extends BaseConsume<?>> UncConsumeType<? extends Type> add(Class<Type> type, ContentType cType){
    return new UncConsumeType<>(type, cType);
  }
  
  public static final UncConsumeType<UncConsumeItems<?>> item = (UncConsumeType<UncConsumeItems<?>>) add(UncConsumeItems.class, ContentType.item);
  public static final UncConsumeType<UncConsumeLiquids<?>> liquid = (UncConsumeType<UncConsumeLiquids<?>>) add(UncConsumeLiquids.class, ContentType.liquid);
  public static final UncConsumeType<UncConsumePower<?>> power = (UncConsumeType<UncConsumePower<?>>) add(UncConsumePower.class, null);
}
