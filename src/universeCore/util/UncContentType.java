package universeCore.util;

import arc.struct.Seq;
import mindustry.ctype.ContentType;
import universeCore.util.handler.ContentHandler;
import universeCore.util.handler.EnumHandler;
import universeCore.util.handler.FieldHandler;

import java.util.ArrayList;
import java.util.Arrays;

/**contentType处理对象，用于创建新的内容类型，以及处理类型的显示排序
 * @author EBwilson */
public class UncContentType{
  private final static EnumHandler<ContentType> handler = new EnumHandler<>(ContentType.class);
  
  public static final Seq<UncContentType> allUncContentType = new Seq<>();
  public static ContentType[] newContentList = new ContentType[0];
  
  public final ContentType value;
  public final int ordinal;
  
  public UncContentType(String name, int ordinal){
    value = handler.addEnumItemTail(name);
    this.ordinal = ordinal;
  
    allUncContentType.add(this);
    
    FieldHandler.setValue(ContentType.class, "all", null, ContentType.values());
    
    ArrayList<ContentType> list = new ArrayList<>(Arrays.asList(ContentType.values()));
    
    for(UncContentType type: allUncContentType){
      list.remove(type.value);
      list.add(type.ordinal, type.value);
    }
    
    newContentList = list.toArray(new ContentType[0]);
  
    ContentHandler.addNewContentType(this);
  }
  
  public UncContentType(String name){
    this(name, ContentType.values().length);
  }
}
