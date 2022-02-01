package universeCore.util.handler;

import arc.Core;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.TechTree;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;
import mindustry.ctype.UnlockableContent;

import java.lang.reflect.Field;
import java.util.Locale;

/**用于操作content的静态方法集合
 * @author EBwilson
 * @since 1.0*/
public class ContentHandler{
  /**用新的content去覆盖一个原有的content
   * @param oldContent 将要被取代的content
   * @param newContent 重写后的content
   * @throws RuntimeException 当oldContent与newContent的类型（ContentType）不相同时抛出*/
  @SuppressWarnings("unchecked")
  public static void overrideContent(MappableContent oldContent, MappableContent newContent){
    Log.info("overriding: " + oldContent + ", " + newContent);
    ContentType type = oldContent.getContentType();
  
    short oldId = oldContent.id;
    String oldName = oldContent.name;
  
    short newId = newContent.id;
    String newName = newContent.name;
  
    FieldHandler.setValue(MappableContent.class, "name", newContent, oldName);
  
    if(oldContent.getContentType() != newContent.getContentType())
      throw new RuntimeException("The old content cannot override by new content, because the content type are different");
  
    if(newContent instanceof UnlockableContent){
      UnlockableContent newC = (UnlockableContent)newContent;
    
      newC.localizedName = Core.bundle.get(type + "." + oldName + ".name", oldName);
      newC.description = Core.bundle.getOrNull(type + "." + oldName + ".description");
      newC.details = Core.bundle.getOrNull(type + "." + oldName + ".details");
      FieldHandler.setValue(UnlockableContent.class, "unlocked", newC, Core.settings != null && Core.settings.getBool(oldName + "-unlocked", false));
    }
  
    try{
      Class<?> contentsClazz = Class.forName("mindustry.content." + type.name().substring(0, 1).toUpperCase(Locale.ROOT) + type.name().substring(1) + "s");
    
      StringBuilder fieldName = new StringBuilder();
      String[] nameParts = oldName.split("-");
      for(int i=0; i<nameParts.length; i++){
        fieldName.append(i > 0? nameParts[i].substring(0, 1).toUpperCase(Locale.ROOT) + nameParts[i].substring(1): nameParts[i]);
      }
    
      Field targetField = contentsClazz.getField(fieldName.toString());
      targetField.set(null, newContent);
    
      ObjectMap<String, MappableContent>[] contentNameMap = FieldHandler.getArray(Vars.content.getClass().getDeclaredField("contentNameMap"), Vars.content, ObjectMap.class);
      Seq<Content>[] contentMap = FieldHandler.getArray(Vars.content.getClass().getDeclaredField("contentMap"), Vars.content, Seq.class);
      newContent.id = oldId;
    
      if(contentMap != null){
        contentMap[type.ordinal()].set(oldId, newContent);
        contentMap[type.ordinal()].remove(newId);
      }
    
      if(contentNameMap != null){
        contentNameMap[type.ordinal()].put(oldName, newContent);
        contentNameMap[type.ordinal()].remove(newName);
      }
      
      if(oldContent instanceof UnlockableContent && newContent instanceof UnlockableContent){
        TechTree.get((UnlockableContent) oldContent).content = (UnlockableContent)newContent;
      }
    }
    catch(Throwable e){
      Log.err(e);
    }
  }
}
