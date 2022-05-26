package universecore.util.ini;

import arc.files.Fi;
import arc.util.Log;

import java.io.*;
import java.util.ArrayList;

/**ini的文件处理工具类*/
public class IniFile{
  public Ini object;
  public Fi file;
  
  protected boolean hasFile = false;
  
  /**用指定的文件创建一个实例，并初始化一个ini解析此文件*/
  public IniFile(Fi file){
    this.file = file;
    hasFile = true;
    parseIni();
  }
  
  /**创建一个ini文件工具，绑定一个ini用于写出*/
  public IniFile(Ini ini){
    this.object = ini;
  }
  
  /**解析当前绑定的ini文件，以获得此ini对象，并绑定到此ini*/
  public Ini parseIni(){
    if(!hasFile || !file.exists()) return null;
    BufferedReader reader = new BufferedReader(file.reader());
    ArrayList<String> strings = new ArrayList<>();
    String line;
    
    try{
      while((line = reader.readLine()) != null) strings.add(line);
    }catch(IOException e){
      Log.err(e);
    }
    
    object = new Ini();
    object.parse(strings.toArray(new String[0]));
    
    return object;
  }
  
  /**设置此ini绑定的文件*/
  public void setFile(Fi file){
    hasFile = true;
    this.file = file;
  }
  
  /**将该ini写出到绑定的文件*/
  public void write(){
    if(hasFile) writeTo(file);
  }
  
  /**将ini写出到指定的文件当中*/
  public void writeTo(Fi file){
    BufferedWriter writer = new BufferedWriter(file.writer(false));
    object.each((section, values) -> {
      try{
        values.write(writer);
        writer.newLine();
      }catch(IOException e){
        Log.err(e);
      }
    });
  }
}
