package universeCore.util.ini;

import arc.func.Cons2;
import arc.struct.ObjectMap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static universeCore.util.ini.IniTypes.*;

/**�ɱ༭��.ini�����ļ��������ߣ��ṩ����ǿ��ini����������Map��Array*/
public class Ini{
  private static final Pattern multiLineMark = Pattern.compile("^[^ ;=]+\\s*[=:]\\s*((|\\{[^}]*)|\\[[^]]*)$");
  private static final Pattern sectionMark = Pattern.compile("^\\[\\w+]$");
  private static final Pattern sectionMatching = Pattern.compile("\\w+");
  
  private static final Pattern k_vPair = Pattern.compile("^[^ ;=]+\\s*[=:]\\s*(\\{.+}|(\\[.+]|([^, =:\\[\\]{}]+)))$");
  
  protected final ObjectMap<String, IniObject> root = new ObjectMap<>();
  
  protected int AnnotationCount = 0;
  
  private IniSection currentSection;
  
  /**�½�һ��iniʵ����Ϊ����*/
  public Ini(){
    currentSection = new IniSection("default");
    root.put("", currentSection);
  }
  
  /**����һ���ַ�������ȡini��ʽ����Ϣ��
   * ������ַ���Ӧ������ȷ�Ļ��з�
   * @param string ���������ַ���*/
  public void parse(String string){
    parse(string.split("\n"));
  }
  
  /**��������ַ��������飬ÿ��Ԫ�ر���Ϊ��һ��
   * ���з���������
   * @param stringLines �ɶ�����Ҫ�������ַ�����ɵ�����*/
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
  
  /**��ȡָ���Ľ��б���ļ�ֵ�����������򷵻�null
   * @param section ��ȡ��ֵ��Ŀ���
   * @param key Ŀ�������*/
  public IniObject get(String section, String key){
    IniSection map = (IniSection) root.get(section);
    if(map != null) return map.get(key);
    return null;
  }
  
  /**��ȡָ���ļ���ini���״γ��ֵ�ֵ
   * @param key ��Ҫ���ҵļ�*/
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
  
  /**������н��е�ָ������ֵ�����Լ����ڵĽ�
   * @param key ��Ҫ���ҵ�Ŀ���
   * @return ����ƥ��ļ�ֵ��ɵ�����*/
  public IniObject[] gets(String key){
    ArrayList<IniObject> list = new ArrayList<>();
    root.each((section, obj) -> {
      IniSection sec = (IniSection) obj;
      if(sec.containsKey(key)) list.add(sec.get(key));
    });
    return list.toArray(new IniObject[0]);
  }
  
  /**��ָ���Ľ���д��һ�Լ�ֵ�ԣ����ڲ��������½��˽�
   * @param section д���Ŀ��ڣ�ָ��Ϊ��default�����߿��ַ���ʱΪĬ�Ͻڣ�λ��ini��ͷ���׸����������Ľ�Ϊֹ��
   * @param key д��ļ�
   * @param value д���ֵ����ʽ����ini��ʽ���Զ�����Ϊini��������*/
  public IniObject put(String section, String key, String value){
    IniObject sec = root.get(section.equals("default")? "": section);
    IniSection map;
    if(sec == null){
      map = new IniSection(section);
    }else map = (IniSection) sec;
    
    return map.put(key, value);
  }
  
  /**���ӽ�д��һ�Լ�ֵ�ԣ����Ǹü��״γ��ֵ�λ�ã��������ڣ�����def���������Ƿ�д��Ĭ�Ͻڣ�λ��ini��ͷ���׸����������Ľ�Ϊֹ��
   * @param key д��ļ�
   * @param value д���ֵ����ʽ����ini��ʽ���Զ�����Ϊini��������
   * @param def ��ʼ�ղ�����ʱ�Ƿ�д�뵽Ĭ�Ͻ�
   * @return д���ֵ*/
  public IniObject put(String key, String value, boolean def){
    IniObject[] result = new IniObject[]{null};
    root.each((section, obj) -> {
      if(result[0] != null) return;
      IniSection map = (IniSection) obj;
      if(map.containsKey(key)) result[0] = map.put(key, value);
    });
  
    return result[0] == null && def? ((IniSection) root.get("")).put(key, value): result[0];
  }
  
  /**���ӽ�д��һ�Լ�ֵ�ԣ����Ǹü��״γ��ֵ�λ�ã��������ڣ���д��Ĭ�Ͻڣ�λ��ini��ͷ���׸����������Ľ�Ϊֹ��
   * @param key д��ļ�
   * @param value д���ֵ����ʽ����ini��ʽ���Զ�����Ϊini��������
   * @return д���ֵ*/
  public IniObject put(String key, String value){
    return put(key, value, true);
  }
  
  /**�����о���ָ�����Ľ�д���ֵ�ԣ���attendΪ�棬�������Ƿ���д˼�������д���ֵ
   * @param key д��ļ�
   * @param value д���ֵ����ʽ����ini��ʽ���Զ�����Ϊini��������
   * @param attend �Ƿ��ڼ�������ʱ׷��
   * @return �ɹ�д�������*/
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
  
  /**�����о���ָ�����Ľ�д���ֵ��
   * @param key д��ļ�
   * @param value д���ֵ����ʽ����ini��ʽ���Զ�����Ϊini��������
   * @return �ɹ�д�������*/
  public int puts(String key, String value){
    return puts(key, value, false);
  }
  
  /**��ָ���Ľ����Ƴ�һ����ֵ��
   * @param section Ŀ���
   * @param key Ҫ�Ƴ��ļ�
   * @return ���Ƴ��ļ�ֵ���������ڣ��򷵻�null*/
  public IniObject remove(String section, String key){
    IniSection map = (IniSection) root.get(section);
    if(map != null) return map.remove(key);
    return null;
  }
  
  /**�Ƴ��׸����ֵ�ƥ���ֵ��
   * @param key Ҫ�Ƴ��ļ�
   * @return ���Ƴ��ļ�ֵ���������ڣ��򷵻�null*/
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
  
  /**�Ƴ�����ƥ��ļ�ֵ��
   * @param key Ҫ�Ƴ��ļ�
   * @return ���Ƴ��ļ�ֵ��ɵ�����*/
  public IniObject[] removes(String key){
    ArrayList<IniObject> list = new ArrayList<>();
    root.each((section, obj) -> {
      IniSection sec = (IniSection) obj;
      if(sec.containsKey(key)) list.add(sec.remove(key));
    });
    return list.toArray(new IniObject[0]);
  }
  
  /**��ȡһ����
   * @param section Ҫ��ȡ�Ľ����ƣ����������š�[]��*/
  public IniSection getSection(String section){
    return (IniSection) root.get(section);
  }
  
  /**��ָ����lambda�������е����е�Ԫ�أ���һ���ǽڣ���ע��������
   * @param cons ���ڱ���ִ�е�lambda����*/
  public void each(Cons2<String, IniObject> cons){
    root.each(cons);
  }
  
  /**��ָ����lambda�������е����нڵ�Ԫ��
   * @param cons ���ڱ���ִ�е�lambda����*/
  public void eachAll(Cons2<String, IniObject> cons){
    root.each((k, v) -> {
      if(v instanceof IniSection)((IniSection)v).each(cons);
    });
  }
  
  public static class IniSection extends IniObject{
    private static final Pattern format = Pattern.compile("^\\w+$");
    private static final Pattern element = Pattern.compile("\\{.+}|[^ ,=:\\[\\]{}]+");
    
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
