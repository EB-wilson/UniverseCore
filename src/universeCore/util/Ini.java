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

/**.ini配置文件解析工具，需要指定文件*/
public class Ini{
  private static final Pattern sectionMark = Pattern.compile("\\[\\w+]");
  
  private final ObjectMap<String, ObjectMap<String, String>> data = new ObjectMap<>();
  private final ArrayList<String> allText = new ArrayList<>();
  
  public final String name;
  
  private Fi file;
  
  private boolean hasFile;
  
  /**用指定的文件创建一个ini解析实例，若其后缀不为.ini则会在目录下新建一个文件作为对象*/
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
  
  /**将ini输出到其指定的文件
   * 可选择是否格式化文本
   * @param format 输出文件是否对文件进行格式化*/
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
  
  /**读取指定键的所有值，无视其所在的节
  * @param key 指定的键
  * @return 键值，若键不存在，则返回null*/
  public String[] get(String key){
    ArrayList<String> result = new ArrayList<>();
    data.each((section, map) -> {
      String value = map.get(key);
      if(value != null) result.add(value);
    });
    return result.toArray(new String[0]);
  }
  
  /**读取指定的键在选定节中的值
  * @param section 指定的节
  * @param key 指定的键
  * @return 键值，若键或节不存在，则返回null*/
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
  
  /**将所有节的指定键设为指定值，若键始终不存在，则将该键添加到[default]节中
  * @param key 指定的键
  * @param value 指定的值*/
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
  
  /**将指定的节中选中的键值设置为指定值，若节或者键不存在，则在末尾新建节，并在节新建该键值
  * @param section 指定的节
  * @param key 指定的键
  * @param value 设定的值*/
  public void put(String section, String key, String value){
    putText(section, key, value);
    if(!data.containsKey(section)){
      data.put(section, new ObjectMap<>());
    }
    data.get(section).put(key, value);
  }
  
  /**获取指定的节中的所有键值对
  * @param section 指定的节
  * @return 返回该节中的所有键值对组成的图*/
  public ObjectMap<String, String> getSection(String section){
    return data.get(section);
  }
  
  public void eachSection(Cons2<String, ObjectMap<String, String>> def){
    data.each(def);
  }
  
  /**获取该ini文件中的所有节与键值对，按ini内的序列顺序排列
  * @return 返回由该ini中所有节与键值对构成的数组，节的划分同ini，完全忽略其他信息(如空格和注释等)*/
  public String[] getAll(){
    return getAll(false);
  }
  
  /**获取该ini文件中的信息
  * @param returnAll 是否一同返回所有信息，无论是否有效，若为真，将返回包括注释和空行在内的所有信息
  * @return 返回由该ini内信息构成的数组，节的划分同ini*/
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
  
  /**获取指定键值对初次出现的节
  * @param key 指定的键
  * @param value 该键的值
  * @return 此键值对所在节的名称，包含[]，若没有找到该键值对，则返回null*/
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
