package universecore.util.handler;

import arc.Core;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.TechTree;
import mindustry.core.ContentLoader;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;
import mindustry.ctype.UnlockableContent;

import java.util.Locale;

/**用于操作content的静态方法集合
 * @author EBwilson
 * @since 1.0*/
public class ContentHandler{
  private static final FieldHandler<ContentLoader> handle = new FieldHandler<>(ContentLoader.class);
  private static final FieldHandler<MappableContent> contHandler = new FieldHandler<>(MappableContent.class);

  private static Seq<Content>[] contentMap;
  private static ObjectMap<String, MappableContent>[] contentNameMap;
  
  private static void updateContainer(){
    contentMap = handle.getValue(Vars.content, "contentMap");
    contentNameMap = handle.getValue(Vars.content, "contentNameMap");
  }
  
  public static void removeContent(Content content){
    updateContainer();
    if(content.getContentType().ordinal() < contentMap.length){
      Seq<Content> conts = contentMap[content.getContentType().ordinal()];
      if(conts != null){
        conts.remove(content);
      }
    }
    
    if(content instanceof MappableContent){
      if(content.getContentType().ordinal() < contentNameMap.length){
        ObjectMap<String, MappableContent> map = contentNameMap[content.getContentType().ordinal()];
        if(map != null){
          map.remove(((MappableContent) content).name);
        }
      }
    }
  }
  
  /**用新的content去覆盖一个原有的content
   * @param oldContent 将要被取代的content
   * @param newContent 重写后的content
   * @throws RuntimeException 当oldContent与newContent的类型（ContentType）不相同时抛出*/
  public static void overrideContent(MappableContent oldContent, MappableContent newContent){
    overrideContent(oldContent, (Class<?>) null, newContent);
  }
  
  public static void overrideContent(MappableContent oldContent, Class<?> declarer, MappableContent newContent){
    updateContainer();
    ContentType type = oldContent.getContentType();
  
    String oldName = oldContent.name;
    String newName = newContent.name;
  
    contHandler.setValue(newContent, "name", oldName);
  
    if(oldContent.getContentType() != newContent.getContentType())
      throw new RuntimeException("The old content cannot override by new content, because the content type are different");
  
    if(newContent instanceof UnlockableContent newC){

      newC.localizedName = Core.bundle.get(type + "." + oldName + ".name", oldName);
      newC.description = Core.bundle.getOrNull(type + "." + oldName + ".description");
      newC.details = Core.bundle.getOrNull(type + "." + oldName + ".details");
      contHandler.setValue(newC, "unlocked", Core.settings != null && Core.settings.getBool(oldName + "-unlocked", false));
    }
  
    try{
      Class<?> contentsClazz = declarer == null ?
          Class.forName("mindustry.content." + type.name().substring(0, 1).toUpperCase(Locale.ROOT) + type.name().substring(1) + "s")
          : declarer;
  
      StringBuilder fieldName = new StringBuilder();
      String[] nameParts = oldName.split("-");
      for(int i=0; i<nameParts.length; i++){
        fieldName.append(i > 0? nameParts[i].substring(0, 1).toUpperCase(Locale.ROOT) + nameParts[i].substring(1): nameParts[i]);
      }
     
      if(contentNameMap != null){
        contentNameMap[type.ordinal()].put(oldName, newContent);
        contentNameMap[type.ordinal()].remove(newName);
      }
      
      overrideContent(oldContent, fieldName.toString(), newContent);
  
      if(oldContent instanceof UnlockableContent && newContent instanceof UnlockableContent){
        TechTree.get((UnlockableContent) oldContent).content = (UnlockableContent)newContent;
      }
    }catch(ClassNotFoundException e){
      throw new RuntimeException(e);
    }
  }
  
  public static void overrideContent(Content oldContent, String srcField, Content newContent){
    updateContainer();
    short oldId = oldContent.id;
    short newId = newContent.id;
  
    if(oldContent.getContentType() != newContent.getContentType())
      throw new RuntimeException("The old content cannot override by new content, because the content type are different");
    
    contHandler.setValue(null, srcField, newContent);
    
    ContentType type = oldContent.getContentType();
    try{
      newContent.id = oldId;
    
      if(contentMap != null){
        contentMap[type.ordinal()].set(oldId, newContent);
        contentMap[type.ordinal()].remove(newId);
      }
    }
    catch(Throwable e){
      Log.err(e);
    }
  }
}
