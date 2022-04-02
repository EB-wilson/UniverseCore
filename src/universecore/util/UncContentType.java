package universecore.util;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;
import universecore.util.handler.EnumHandler;
import universecore.util.handler.FieldHandler;

import java.util.ArrayList;
import java.util.Arrays;

/**contentType处理对象，用于创建新的内容类型，以及处理类型的显示排序
 * @author EBwilson
 * @since 1.0*/
public class UncContentType{
  private final static EnumHandler<ContentType> handler = new EnumHandler<>(ContentType.class);
  
  public static final Seq<UncContentType> allUncContentType = new Seq<>();
  public static ContentType[] displayContentList = new ContentType[0];
  
  public final ContentType value;
  public final int ordinal;

  public final boolean display;

  public UncContentType(String name, int ordinal){
    this(name, ordinal, true);
  }
  
  public UncContentType(String name, int ordinal, boolean display){
    value = handler.addEnumItemTail(name);
    this.ordinal = ordinal;
    this.display = display;
  
    allUncContentType.add(this);
    
    FieldHandler.setValue(ContentType.class, "all", null, ContentType.values());
    
    ArrayList<ContentType> list = new ArrayList<>(Arrays.asList(ContentType.values()));
    
    for(UncContentType type: allUncContentType){
      if(!type.display) continue;
      list.remove(type.value);
      list.add(type.ordinal, type.value);
    }
    
    displayContentList = list.toArray(new ContentType[0]);
  
    try{
      ObjectMap<String, MappableContent>[] contentNameMap = FieldHandler.getArray(Vars.content.getClass().getDeclaredField("contentNameMap"), Vars.content, ObjectMap.class);
      Seq<Content>[] contentMap = FieldHandler.getArray(Vars.content.getClass().getDeclaredField("contentMap"), Vars.content, Seq.class);
    
      ArrayList<ObjectMap<String, MappableContent>> contentNameMapList = new ArrayList<>(Arrays.asList(contentNameMap));
      ArrayList<Seq<Content>> contentMapList = new ArrayList<>(Arrays.asList(contentMap));
    
      contentNameMapList.add(value.ordinal(), new ObjectMap<>());
      contentMapList.add(value.ordinal(), new Seq<>());
    
      FieldHandler.setValue(Vars.content.getClass(), "contentNameMap", Vars.content, contentNameMapList.toArray(new ObjectMap[0]));
      FieldHandler.setValue(Vars.content.getClass(), "contentMap", Vars.content, contentMapList.toArray(new Seq[0]));
    }
    catch(Throwable e){
      Log.err(e);
    }
  }
  
  public UncContentType(String name){
    this(name, ContentType.values().length);
  }
}
