package universecore.world.consumers;

import arc.struct.Seq;
import mindustry.ctype.ContentType;

@SuppressWarnings("unchecked")
public class ConsumeType<T extends BaseConsume<?>>{
  private static final Seq<ConsumeType<?>> allType = new Seq<>();
  private final int id;
  private final Class<T> type;
  private final ContentType contType;
  
  public ConsumeType(Class<T> type, ContentType cType){
    id = allType.size;
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
  
  public static ConsumeType<?>[] all(){
    return allType.toArray(ConsumeType.class);
  }
  
  public static <Type extends BaseConsume<?>> ConsumeType<? extends Type> add(Class<Type> type, ContentType cType){
    return new ConsumeType<>(type, cType);
  }

  public static final ConsumeType<ConsumePower<?>> power = (ConsumeType<ConsumePower<?>>) add(ConsumePower.class, null);
  public static final ConsumeType<ConsumeItemBase<?>> item = (ConsumeType<ConsumeItemBase<?>>) add(ConsumeItemBase.class, ContentType.item);
  public static final ConsumeType<ConsumeLiquidBase<?>> liquid = (ConsumeType<ConsumeLiquidBase<?>>) add(ConsumeLiquidBase.class, ContentType.liquid);
  public static final ConsumeType<ConsumePayload<?>> payload = (ConsumeType<ConsumePayload<?>>) add(ConsumePayload.class, null);
}
