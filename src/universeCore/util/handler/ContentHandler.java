package universeCore.util.handler;

import arc.Core;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;
import mindustry.ctype.UnlockableContent;
import universeCore.util.UncContentType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ContentHandler{
  /**���ڸ���һ��ԭ�е�content��������Ҫ�������¼��ؿƼ���
   *
   * @param oldContent ��Ҫ��ȡ����content
   * @param newContent ��д���content
   *
   * @throws RuntimeException ��oldContent��newContent�����ͣ�ContentType������ͬʱ�׳�*/
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
    }
    catch(Throwable e){
      Log.err(e);
    }
  }
  
  @SuppressWarnings("unchecked")
  public static void addNewContentType(UncContentType newType){
    try{
      ObjectMap<String, MappableContent>[] contentNameMap = FieldHandler.getArray(Vars.content.getClass().getDeclaredField("contentNameMap"), Vars.content, ObjectMap.class);
      Seq<Content>[] contentMap = FieldHandler.getArray(Vars.content.getClass().getDeclaredField("contentMap"), Vars.content, Seq.class);
  
      assert contentNameMap != null;
      ArrayList<ObjectMap<String, MappableContent>> contentNameMapList = new ArrayList<>(Arrays.asList(contentNameMap));
      assert contentMap != null;
      ArrayList<Seq<Content>> contentMapList = new ArrayList<>(Arrays.asList(contentMap));
      
      contentNameMapList.add(newType.value.ordinal(), new ObjectMap<>());
      contentMapList.add(newType.value.ordinal(), new Seq<>());
      
      FieldHandler.setValue(Vars.content.getClass(), "contentNameMap", Vars.content, contentNameMapList.toArray());
      FieldHandler.setValue(Vars.content.getClass(), "contentMap", Vars.content, contentMapList.toArray());
    }
    catch(Throwable e){
      Log.err(e);
    }
  }
}
