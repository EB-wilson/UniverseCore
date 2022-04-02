package universecore.util;

import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;

import static universecore.util.handler.ContentHandler.*;

/**内容覆盖器实现此接口，用于在执行load()方法中进行内容覆盖
 * @author EBwilson */
public interface OverrideContentList extends ContentList{
  default void doOverrideContent(ContentType type, short id, MappableContent newContent){
    overrideContent(Vars.content.getByID(type, id), newContent);
  }
  
  default void doOverrideContent(ContentType type, String name, MappableContent newContent){
    overrideContent(Vars.content.getByName(type, name), newContent);
  }
  
  default void doOverrideContent(MappableContent oldContent, MappableContent newContent){
    overrideContent(oldContent, newContent);
  }
}
