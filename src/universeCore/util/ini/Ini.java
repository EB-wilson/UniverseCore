package universeCore.util.ini;

import arc.func.Cons2;
import arc.struct.ObjectMap;
import arc.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static universeCore.util.ini.IniTypes.*;

/**可编辑的.ini配置文件解析工具，提供了增强的ini数据类型如Map和Array*/
public class Ini{
  private static final Pattern multiLineMark = Pattern.compile("^[^ ;=]+\\s*[=:]\\s*((|\\{[^}]*)|\\[[^]]*)$");
  private static final Pattern sectionMark = Pattern.compile("^\\[\\w+]$");
  private static final Pattern sectionMatching = Pattern.compile("\\w+");
  
  private static final Pattern k_vPair = Pattern.compile("^[^ ;=]+\\s*[=:]\\s*(\\{.+\\}|(\\[.+]|([^, =:\\[\\]{}]+)))$");
  
  protected final ObjectMap<String, IniObject> root = new ObjectMap<>();
  
  protected int AnnotationCount = 0;
  
  private IniSection currentSection;
  
  /**新建一个ini实例作为容器*/
  public Ini(){
    currentSection = new IniSection("default");
    root.put("", currentSection);
  }
  
  /**分析一段字符串，读取ini格式化信息。
   * 输入的字符串应该有正确的换行符
   * @param string 待解析的字符串*/
  public void parse(String string){
    parse(string.split("\n"));
  }
  
  /**分析多个字符串的数组，每个元素被认为是一行
   * 换行符将被忽略
   * @param stringLines 由多行需要解析的字符串组成的数组*/
  public void parse(String[] stringLines){
    StringBuilder multiLineBuffer = new StringBuilder();
    boolean buffering = false;
    for(String line: stringLines){
      String currStr = line.replace("\n", " ").trim();
      
      if(buffering){
        if(IniAnnotation.is(currStr)) continue;
        if(!sectionMark.matcher(currStr).matches() && !k_vPair.matcher(currStr).matches()){
          multiLineBuffer.append(currStr);
          continue;
        }
        else{
          buffering = false;
          currStr = multiLineBuffer.toString();
          multiLineBuffer = new StringBuilder();
        }
      }
  
      if(sectionMark.matcher(currStr).matches()){
        Matcher matcher = sectionMatching.matcher(currStr);
        if(matcher.find()) currentSection = new IniSection(matcher.group());
        root.put(currentSection.name, currentSection);
        continue;
      }
  
      if(IniAnnotation.is(currStr)){
        root.put("@anno#" + AnnotationCount++, new IniAnnotation(currStr));
        continue;
      }
  
      if(k_vPair.matcher(currStr).matches()){
        currentSection.put(currStr);
        continue;
      }
      
      if(multiLineMark.matcher(currStr).matches()){
        buffering = true;
        multiLineBuffer.append(currStr);
      }
    }
  }
  
  /**获取指定的节中保存的键值，若不存在则返回null
   * @param section 获取键值的目标节
   * @param key 目标键名称*/
  public IniObject get(String section, String key){
    IniSection map = (IniSection) root.get(section);
    if(map != null) return map.get(key);
    return null;
  }
  
  /**获取指定的键在ini中首次出现的值
   * @param key 需要查找的键*/
  public IniObject get(String key){
    IniObject[] first = new IniObject[]{null};
    root.each((section, obj) -> {
      if(first[0] != null || !(obj instanceof IniSection)) return;
      IniSection sec = (IniSection) obj;
      if(sec.containsKey(key)){
        first[0] = sec.get(key);
      }
    });
    return first[0];
  }
  
  /**获得所有节中的指定键键值，忽略键所在的节
   * @param key 需要查找的目标键
   * @return 所有匹配的键值组成的数组*/
  public IniObject[] gets(String key){
    ArrayList<IniObject> list = new ArrayList<>();
    root.each((section, obj) -> {
      IniSection sec = (IniSection) obj;
      if(sec.containsKey(key)) list.add(sec.get(key));
    });
    return list.toArray(new IniObject[0]);
  }
  
  /**在指定的节中写入一对键值对，若节不存在则新建此节
   * @param section 写入的目标节，指定为“default”或者空字符串时为默认节（位于ini开头到首个字面声明的节为止）
   * @param key 写入的键
   * @param value 写入的值，格式按照ini格式，自动解析为ini数据类型*/
  public IniObject put(String section, String key, String value){
    IniObject sec = root.get(section.equals("default")? "": section);
    IniSection map;
    if(sec == null){
      map = new IniSection(section);
    }else map = (IniSection) sec;
    
    return map.put(key, value);
  }
  
  /**无视节写入一对键值对，覆盖该键首次出现的位置，若不存在，根据def参数决定是否写入默认节（位于ini开头到首个字面声明的节为止）
   * @param key 写入的键
   * @param value 写入的值，格式按照ini格式，自动解析为ini数据类型
   * @param def 键始终不存在时是否写入到默认节
   * @return 写入的值*/
  public IniObject put(String key, String value, boolean def){
    IniObject[] result = new IniObject[]{null};
    root.each((section, obj) -> {
      if(result[0] != null) return;
      IniSection map = (IniSection) obj;
      if(map.containsKey(key)) result[0] = map.put(key, value);
    });
  
    return result[0] == null && def? ((IniSection) root.get("")).put(key, value): result[0];
  }
  
  /**无视节写入一对键值对，覆盖该键首次出现的位置，若不存在，则写入默认节（位于ini开头到首个字面声明的节为止）
   * @param key 写入的键
   * @param value 写入的值，格式按照ini格式，自动解析为ini数据类型
   * @return 写入的值*/
  public IniObject put(String key, String value){
    return put(key, value, true);
  }
  
  /**对所有具有指定键的节写入键值对，若attend为真，则无论是否具有此键，都会写入键值
   * @param key 写入的键
   * @param value 写入的值，格式按照ini格式，自动解析为ini数据类型
   * @param attend 是否在键不存在时追加
   * @return 成功写入的数量*/
  public int puts(String key, String value, boolean attend){
    AtomicInteger counter = new AtomicInteger();
    root.each((section, obj) -> {
      IniSection map = (IniSection)obj;
      if(attend){
        map.put(key, value);
        counter.getAndIncrement();
      }
      else if(map.containsKey(key)){
        map.put(key, value);
        counter.getAndIncrement();
      }
    });
    return counter.get();
  }
  
  /**对所有具有指定键的节写入键值对
   * @param key 写入的键
   * @param value 写入的值，格式按照ini格式，自动解析为ini数据类型
   * @return 成功写入的数量*/
  public int puts(String key, String value){
    return puts(key, value, false);
  }
  
  /**从指定的节中移除一个键值对
   * @param section 目标节
   * @param key 要移除的键
   * @return 被移除的键值，若不存在，则返回null*/
  public IniObject remove(String section, String key){
    IniSection map = (IniSection) root.get(section);
    if(map != null) return map.remove(key);
    return null;
  }
  
  /**移除首个出现的匹配键值对
   * @param key 要移除的键
   * @return 被移除的键值，若不存在，则返回null*/
  public IniObject remove(String key){
    IniObject[] first = new IniObject[]{null};
    root.each((section, obj) -> {
      if(first[0] != null || !(obj instanceof IniSection)) return;
      IniSection sec = (IniSection) obj;
      if(sec.containsKey(key)){
        first[0] = sec.remove(key);
      }
    });
    return first[0];
  }
  
  /**移除所有匹配的键值对
   * @param key 要移除的键
   * @return 被移除的键值组成的数组*/
  public IniObject[] removes(String key){
    ArrayList<IniObject> list = new ArrayList<>();
    root.each((section, obj) -> {
      IniSection sec = (IniSection) obj;
      if(sec.containsKey(key)) list.add(sec.remove(key));
    });
    return list.toArray(new IniObject[0]);
  }
  
  /**获取一个节
   * @param section 要获取的节名称，不含方括号“[]”*/
  public IniSection getSection(String section){
    return (IniSection) root.get(section);
  }
  
  /**用指定的lambda遍历根中的所有的元素，不一定是节，请注意检查类型
   * @param cons 用于遍历执行的lambda函数*/
  public void each(Cons2<String, IniObject> cons){
    root.each(cons);
  }
  
  /**用指定的lambda遍历根中的所有节的元素
   * @param cons 用于遍历执行的lambda函数*/
  public void eachAll(Cons2<String, IniSection> cons){
    root.each((k, v) -> {
      if(v instanceof IniSection)((IniSection)v).each((name, value) -> cons.get(name, (IniSection)value));
    });
  }
  
  public static class IniSection extends IniObject{
    private static final Pattern format = Pattern.compile("^\\w+$");
    private static final Pattern element = Pattern.compile("\\{.+\\}|[^ ,=:\\[\\]{}]+");
    
    protected final ObjectMap<String, IniObject> child = new ObjectMap<>();
    
    public final String name;
    
    public IniSection(String name){
      super(name);
      this.name = name;
    }
    
    public IniObject put(String key, String value){
      return put(key + " = " + value);
    }
    
    public IniObject put(String string){
      string = string.trim();
      if(k_vPair.matcher(string).matches()){
        Matcher elementMatcher = element.matcher(string);
        String key = elementMatcher.find()? elementMatcher.group(): "";
        String value = elementMatcher.find()? elementMatcher.group(): "";
  
        IniObject obj = testType(value);
        child.put(key, obj);
        return obj;
      }
      throw new StringFormatException("try put a non-key-value pair data to section: \"" + name + "\"");
    }
    
    public IniObject get(String key){
      return child.get(key);
    }
    
    public IniObject remove(String key){
      return child.remove(key);
    }
    
    public boolean containsKey(String key){
      return child.containsKey(key);
    }
  
    @Override
    public Pattern formatPattern(){
      return format;
    }
  
    @Override
    public Pattern matcherPattern(){
      return sectionMatching;
    }
  
    public void each(Cons2<String, IniObject> cons){
      child.each(cons);
    }
  
    @Override
    public ObjectMap<String, IniObject> get(){
      return child;
    }
  
    @Override
    public Class<?> type(){
      return ObjectMap.class;
    }
  
    @Override
    public void write(BufferedWriter writer) throws IOException{
      writer.write("[" + name + "]");
      writer.newLine();
      for(ObjectMap.Entry<String, IniObject> entry : child){
        writer.write(entry.key);
        writer.write(" = ");
        entry.value.write(writer);
        writer.newLine();
      }
    }
  }
  
  public static class StringFormatException extends RuntimeException{
    public StringFormatException(String string){
      super(string);
    }
  }
}
