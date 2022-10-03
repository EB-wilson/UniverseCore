package universecore.util;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.core.ContentLoader;
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
  private final static FieldHandler<ContentLoader> fieldHandler = new FieldHandler<>(ContentLoader.class);
  private final static EnumHandler<ContentType> handler = new EnumHandler<>(ContentType.class);
  
  public static final ObjectMap<ContentType, UncContentType> allUncContentType = new ObjectMap<>();

  static {
    for(ContentType value: ContentType.values()){
      allUncContentType.put(value, new UncContentType(value));
    }
  }

  public static ContentType[] displayContentList = new ContentType[0];
  
  public final ContentType value;
  public final int ordinal;

  public final boolean display;

  private UncContentType(ContentType type){
    this.value = type;
    this.ordinal = type.ordinal();
    this.display = true;
  }

  public UncContentType(String name, Class<? extends Content> contentClass){
    this(name, ContentType.values().length, contentClass);
  }

  public UncContentType(String name, int ordinal, Class<? extends Content> contentClass){
    this(name, ordinal, contentClass, true);
  }
  
  public UncContentType(String name, int ordinal, Class<? extends Content> contentClass, boolean display){
    value = handler.addEnumItemTail(name, contentClass);
    this.ordinal = ordinal;
    this.display = display;
  
    allUncContentType.put(value, this);
    
    FieldHandler.setValueDefault(ContentType.class, "all", ContentType.values());

    reloadDisplay();
  
    try{
      ObjectMap<String, MappableContent>[] contentNameMap = fieldHandler.getValue( Vars.content, "contentNameMap");
      Seq<Content>[] contentMap = fieldHandler.getValue(Vars.content, "contentMap");
    
      ArrayList<ObjectMap<String, MappableContent>> contentNameMapList = new ArrayList<>(Arrays.asList(contentNameMap));
      ArrayList<Seq<Content>> contentMapList = new ArrayList<>(Arrays.asList(contentMap));
    
      contentNameMapList.add(value.ordinal(), new ObjectMap<>());
      contentMapList.add(value.ordinal(), new Seq<>());
    
      fieldHandler.setValue(Vars.content, "contentNameMap", contentNameMapList.toArray(new ObjectMap[0]));
      fieldHandler.setValue(Vars.content, "contentMap", contentMapList.toArray(new Seq[0]));
    }
    catch(Throwable e){
      Log.err(e);
    }
  }

  protected void reloadDisplay(){
    Seq<ContentType> list = new Seq<>();

    for(ContentType type: ContentType.values()){
      UncContentType t = allUncContentType.get(type);
      if(!t.display) continue;
      list.insert(t.ordinal, t.value);
    }

    displayContentList = list.toArray(ContentType.class);
  }
}
