package universeCore.util;

import arc.files.Fi;
import arc.func.Cons2;
import arc.struct.ObjectMap;
import arc.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**.ini�����ļ��������ߣ���Ҫָ���ļ�*/
public class Ini{
  private static final Pattern sectionMark = Pattern.compile("\\[\\w+]");
  
  private final ObjectMap<String, ObjectMap<String, String>> data = new ObjectMap<>();
  private final ArrayList<String> allText = new ArrayList<>();
  
  public final String name;
  
  private Fi file;
  
  private boolean hasFile;
  
  /**��ָ�����ļ�����һ��ini����ʵ���������׺��Ϊ.ini�����Ŀ¼���½�һ���ļ���Ϊ����*/
  public Ini(Fi file){
    this.name = file.name();
    setFile(file);
    
    load();
  }
  
  public Ini(String name){
    this.name = name;
    hasFile = false;
  }
  
  public void setFile(Fi file){
    hasFile = true;
    if(!file.extension().equals("ini")){
      this.file = file.parent().child(file.name().split("\\.")[0] + ".ini");
      return;
    }
    this.file = file;
  }
  
  public Fi getFile(){
    return file;
  }
  
  public void load(){
    data.clear();
    
    String section = "default";
    data.put(section, new ObjectMap<>());
    
    try{
      try(BufferedReader reader = new BufferedReader(file.reader())){
        String strLine;
        while((strLine = reader.readLine()) != null){
          allText.add(strLine);
          strLine = strLine.trim();
  
          if(strLine.isEmpty() || strLine.startsWith("#") || strLine.startsWith(";")) continue;
          
          if(sectionMark.matcher(strLine).matches()){
            section = strLine.replace("[", "").replace("]", "").trim();
            if(!data.containsKey(section)) data.put(section, new ObjectMap<>());
            continue;
          }
          
          int first = strLine.indexOf("=");
          if(first == -1) first = strLine.indexOf(":");
          if(first == -1) continue;
          
          String key = strLine.substring(0, first).trim(), value = strLine.substring(first+1).trim();
          data.get(section).put(key, value);
        }
      }
    }catch(IOException e){
      Log.err(e);
    }
  }
  
  /**��ini�������ָ�����ļ�
   * ��ѡ���Ƿ��ʽ���ı�
   * @param format ����ļ��Ƿ���ļ����и�ʽ��*/
  public void writeToFile(boolean format){
    if(format){
      ObjectMap<String, String> defaults = data.remove("default");
  
      try{
        try(BufferedWriter writer = new BufferedWriter(file.writer(false))){
          defaults.each((k, v) -> {
            try{
              writer.write(k + " = " + v);
              writer.newLine();
            }catch(IOException e){
              Log.err(e);
            }
          });
      
          AtomicReference<String> string = new AtomicReference<>();
          data.each((section, map) -> {
            try{
              if(!section.equals(string.get())){
                writer.write(section);
                writer.newLine();
                string.set(section);
              }
              map.each((k, v) -> {
                try{
                  writer.write(k + " = " + v);
                  writer.newLine();
                }catch(IOException e){
                  Log.err(e);
                }
              });
            }catch(IOException e){
              Log.err(e);
            }
          });
        }
      }catch(IOException e){
        Log.err(e);
      }
    }
    else{
      try{
        try(BufferedWriter writer = new BufferedWriter(file.writer(false))){
          for(String textLine: allText){
            writer.write(textLine);
            writer.newLine();
          }
        }
      }
      catch(IOException e){
        Log.err(e);
      }
    }
  }
  
  /**��ȡָ����������ֵ�����������ڵĽ�
  * @param key ָ���ļ�
  * @return ��ֵ�����������ڣ��򷵻�null*/
  public String[] get(String key){
    ArrayList<String> result = new ArrayList<>();
    data.each((section, map) -> {
      String value = map.get(key);
      if(value != null) result.add(value);
    });
    return result.toArray(new String[0]);
  }
  
  /**��ȡָ���ļ���ѡ�����е�ֵ
  * @param section ָ���Ľ�
  * @param key ָ���ļ�
  * @return ��ֵ��������ڲ����ڣ��򷵻�null*/
  public String get(String section, String key){
    return data.get(section).get(key);
  }
  
  private void putText(String section, String key, String value){
    int lineMark = 0;
    if(section.equals("default")){
      for(int l = 0; l < allText.size(); l++){
        String str = allText.get(l).trim();
  
        int first = str.indexOf("=");
        if(first == -1) first = str.indexOf(":");
        if(first == -1) continue;
  
        if(str.substring(0, first).trim().equals(key)){
          allText.set(l, str.substring(0, first + 1) + " = " + value);
          return;
        }
        
        if(!str.equals("") && !str.startsWith("#") && !str.startsWith(";")){
          lineMark = l;
        }
        if(sectionMark.matcher(str).matches() || l == allText.size()-1){
          allText.add(lineMark + 1, key + " = " + value);
          return;
        }
      }
    }
    else{
      for(int i=0; i<allText.size(); i++){
        String str = allText.get(i).trim();
    
        boolean inSection = sectionMark.matcher(str).matches() && section.equals(str.replace("[", "").replace("]", "").trim());
    
        int first = str.indexOf("=");
        if(first == -1) first = str.indexOf(":");
        if(first == -1) continue;
    
        if(inSection && str.substring(0, first).trim().equals(key)){
          allText.set(i, str.substring(0, first + 1) + " = " + value);
          return;
        }
        
        if(i == allText.size() - 1){
          if(str.equals("")){
            allText.add("");
          }
          allText.add("[" + section + "]");
          allText.add(key + " = " + value);
        }
      }
    }
  }
  
  /**�����нڵ�ָ������Ϊָ��ֵ������ʼ�ղ����ڣ��򽫸ü���ӵ�[default]����
  * @param key ָ���ļ�
  * @param value ָ����ֵ*/
  public void put(String key, String value){
    boolean[] bool = {false};
    
    data.each((section, map) -> {
      if(map.containsKey(key)){
        map.put(key, value);
        putText(section, key, value);
        bool[0] = true;
      }
    });
    
    if(!bool[0]){
      data.get("default").put(key,value);
      putText("default", key, value);
    }
  }
  
  /**��ָ���Ľ���ѡ�еļ�ֵ����Ϊָ��ֵ�����ڻ��߼������ڣ�����ĩβ�½��ڣ����ڽ��½��ü�ֵ
  * @param section ָ���Ľ�
  * @param key ָ���ļ�
  * @param value �趨��ֵ*/
  public void put(String section, String key, String value){
    putText(section, key, value);
    if(!data.containsKey(section)){
      data.put(section, new ObjectMap<>());
    }
    data.get(section).put(key, value);
  }
  
  /**��ȡָ���Ľ��е����м�ֵ��
  * @param section ָ���Ľ�
  * @return ���ظý��е����м�ֵ����ɵ�ͼ*/
  public ObjectMap<String, String> getSection(String section){
    return data.get(section);
  }
  
  public void eachSection(Cons2<String, ObjectMap<String, String>> def){
    data.each(def);
  }
  
  /**��ȡ��ini�ļ��е����н����ֵ�ԣ���ini�ڵ�����˳������
  * @return �����ɸ�ini�����н����ֵ�Թ��ɵ����飬�ڵĻ���ͬini����ȫ����������Ϣ(��ո��ע�͵�)*/
  public String[] getAll(){
    return getAll(false);
  }
  
  /**��ȡ��ini�ļ��е���Ϣ
  * @param returnAll �Ƿ�һͬ����������Ϣ�������Ƿ���Ч����Ϊ�棬�����ذ���ע�ͺͿ������ڵ�������Ϣ
  * @return �����ɸ�ini����Ϣ���ɵ����飬�ڵĻ���ͬini*/
  public String[] getAll(boolean returnAll){
    ArrayList<String> temp = new ArrayList<>();
    try{
      try(BufferedReader reader = new BufferedReader(file.reader())){
        String strLine;
        while((strLine = reader.readLine()) != null){
          strLine = strLine.trim();
  
          int first = strLine.indexOf("=");
          if(first == -1) first = strLine.indexOf(":");
          if(!returnAll && (first == -1 || strLine.startsWith("#") || strLine.startsWith(";") || strLine.isEmpty())) continue;
          
          temp.add(strLine);
        }
      }
      return temp.toArray(new String[0]);
    }catch(IOException e){
      Log.err(e);
      return null;
    }
  }
  
  /**��ȡָ����ֵ�Գ��γ��ֵĽ�
  * @param key ָ���ļ�
  * @param value �ü���ֵ
  * @return �˼�ֵ�����ڽڵ����ƣ�����[]����û���ҵ��ü�ֵ�ԣ��򷵻�null*/
  public String findSection(String key, String value){
    AtomicReference<String> result = new AtomicReference<>(null);
    data.each((sec, map) -> {
      map.each((k, v) -> {
        if(key.equals(k) && value.equals(v)){
          result.set(sec);
        }
      });
    });
    
    return result.get();
  }
}
