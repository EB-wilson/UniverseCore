package universecore.util.ini;

import arc.func.Cons2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class IniTypes{
  public static IniObject testType(String test){
    if(IniAnnotation.is(test)) return new IniAnnotation(test);
    if(IniNumber.is(test)) return new IniNumber(test);
    if(IniBoolean.is(test)) return new IniBoolean(test);
    if(IniArray.is(test)) return new IniArray(test);
    if(IniMap.is(test)) return new IniMap(test);
    if(IniDataStructure.is(test)) return new IniDataStructure(test);
    return new IniObject(test);
  }
  
  public static class IniObject{
    private static final Pattern format = Pattern.compile(".+");
    
    protected Matcher matchResult;
    
    protected String data;
    
    public IniObject(String string){
      data = string.trim();
      matchResult = matcherPattern().matcher(data);
      if(!formatPattern().matcher(data).matches())
        throw new Ini.StringFormatException("this type require format with \"" + type().getTypeName() + "\", but input is \"" + testType(string).type().getTypeName() + "\"");
    }
    
    public static boolean is(String string){
      return format.matcher(string).matches();
    }
    
    public <T> T get(){
      return (T) data;
    }
    
    public Class<?> type(){
      return String.class;
    }
    
    public Pattern formatPattern(){
      return format;
    }
    
    public Pattern matcherPattern(){
      return format;
    }
    
    public boolean childValid(IniObject target){
      return target instanceof IniDataStructure || target instanceof IniArray || target instanceof IniMap;
    }
    
    public void write(BufferedWriter writer) throws IOException{
      writer.write(data);
    }
  }
  
  public static class IniMap extends IniObject{
    private static final Pattern format = Pattern.compile("^\\{[^ ,=:{}]+\\s*[:=]\\s*([^ ,=:{}]*|\\[[^ ,{}]+(\\s*,\\s*?[^ ,{}]+)*\\s*,?\\s*])+(\\s*,\\s*[^ ,=:{}]+\\s*[:=]\\s*([^ ,=:{}]+|\\[[^ ,{}]+(\\s*,\\s*?[^ ,{}]+)*\\s*,?\\s*]))*\\s*,?\\s*\\}$");
    private static final Pattern matcher = Pattern.compile("[^ ,=:{}]+\\s*[:=]\\s*(\\([^ ,{}\\[\\]()]+(\\s*,\\s*?[^ ,{}\\[\\]()]+)*\\s*,?\\s*\\)|(\\[[^ ,{}]+(\\s*,\\s*?[^ ,{}]+)*\\s*,?\\s*]|[^ ,=:{}\\[\\]]+))");
    
    private static final Pattern element = Pattern.compile("\\[.+]|[^ ,=:\\[\\]{}]+");
    
    protected HashMap<String, IniObject> map = new HashMap<>();
    
    public IniMap(String string){
      super(string);
      while(matchResult.find()){
        String str = matchResult.group();
        Matcher matching = element.matcher(str);
        String key = matching.find()? matching.group(): "";
        String value = matching.find()? matching.group(): "";
        
        map.put(key, testType(value));
      }
    }
  
    public static boolean is(String string){
      return format.matcher(string).matches();
    }
    
    public void each(Cons2<String, IniObject> cons){
      map.forEach(cons::get);
    }
  
    @Override
    public HashMap<String, IniObject> get(){
      return map;
    }
    
    @Override
    public Class<?> type(){
      return HashMap.class;
    }
  
    @Override
    public Pattern formatPattern(){
      return format;
    }
    
    @Override
    public Pattern matcherPattern(){
      return matcher;
    }
  
    @Override
    public void write(BufferedWriter writer) throws IOException{
      writer.write("{");
      AtomicBoolean first = new AtomicBoolean(true);
      map.forEach((k, v) -> {
        try{
          if(! first.get()) writer.write(", ");
          writer.write(k);
          writer.write(" = ");
          v.write(writer);
        }catch(IOException e){
          e.printStackTrace();
        }
        first.set(false);
      });
      writer.write("}");
    }
  }
  
  public static class IniArray extends IniObject implements Iterable<IniObject>{
    private static final Pattern format = Pattern.compile("^\\[[^ ,{}\\[\\]]+(\\s*,\\s*?[^ ,{}\\[\\]]+)*\\s*,?\\s*]$");
    private static final Pattern matcher = Pattern.compile("\\([^ ,{}\\[\\]()]+(\\s*,\\s*?[^ ,{}\\[\\]()]+)*\\s*,?\\s*\\)|[^ \\[\\],{}]+");
    
    protected ArrayList<IniObject> items;
    
    public IniArray(String string){
      super(string);
      items = new ArrayList<>();
      while(matchResult.find()){
        items.add(testType(matchResult.group()));
      }
    }
  
    public static boolean is(String string){
      return format.matcher(string).matches();
    }
    
    public void each(Consumer<IniObject> cons){
      items.forEach(cons);
    }
  
    @Override
    public ArrayList<IniObject> get(){
      return items;
    }
    
    @Override
    public Class<?> type(){
      return ArrayList.class;
    }
  
    @Override
    public Pattern formatPattern(){
      return format;
    }
    
    @Override
    public Pattern matcherPattern(){
      return matcher;
    }
  
    @Override
    public void write(BufferedWriter writer) throws IOException{
      writer.write("[");
      boolean first = true;
      for(IniObject object: items){
        if(!first) writer.write(", ");
        object.write(writer);
        first = false;
      }
      writer.write("]");
    }
  
    @Override
    public Iterator<IniObject> iterator(){
      return items.iterator();
    }
  }
  
  public static class IniDataStructure extends IniObject{
    private static final Pattern format = Pattern.compile("^\\([^ ,{}\\[\\]]+(\\s*,\\s*?[^ ,{}\\[\\]]+)*\\s*,?\\s*\\)$");
    private static final Pattern matcher = Pattern.compile("[^ ,{}\\[\\]()]+");
  
    protected ArrayList<IniObject> items;
    
    public IniDataStructure(String string){
      super(string);
      items = new ArrayList<>();
      while(matchResult.find()){
        items.add(testType(matchResult.group()));
      }
    }
  
    public static boolean is(String string){
      return format.matcher(string).matches();
    }
  
    @Override
    public ArrayList<IniObject> get(){
      return items;
    }
  
    @Override
    public Class<?> type(){
      return ArrayList.class;
    }
  
    @Override
    public Pattern formatPattern(){
      return format;
    }
  
    @Override
    public Pattern matcherPattern(){
      return matcher;
    }
  
    @Override
    public void write(BufferedWriter writer) throws IOException{
      writer.write("(");
      boolean first = true;
      for(IniObject object: items){
        if(!first) writer.write(", ");
        object.write(writer);
        first = false;
      }
      writer.write(")");
    }
  }
  
  public static class IniNumber extends IniObject{
    private static final Pattern format = Pattern.compile("^(\\d+.\\d+)$");
    
    protected Number number;
    
    public IniNumber(String string){
      super(string);
      if(matchResult.find()){
        String str = matchResult.group();
        if(str.contains(".")){
          number = Double.valueOf(str);
        }
        else{
          number = str.length() >= 10? Long.parseLong(str): Integer.parseInt(str);
        }
      }
    }
  
    public static boolean is(String string){
      return format.matcher(string).matches();
    }
  
    @Override
    public Number get(){
      return number;
    }
  
    @Override
    public Class<?> type(){
      return Number.class;
    }
  
    public byte byteValue(){
      return number.byteValue();
    }
    
    public short shortValue(){
      return number.shortValue();
    }
    
    public float floatValue(){
      return number.floatValue();
    }
    
    public int intValue(){
      return number.intValue();
    }
    
    public double doubleValue(){
      return number.doubleValue();
    }
    
    public long longValue(){
      return number.longValue();
    }
    
    @Override
    public Pattern formatPattern(){
      return format;
    }
    
    @Override
    public Pattern matcherPattern(){
      return format;
    }
  
    @Override
    public void write(BufferedWriter writer) throws IOException{
      writer.write(number.toString());
    }
  }
  
  public static class IniBoolean extends IniObject{
    private static final Pattern format = Pattern.compile("^(false|true)$");
    
    protected Boolean value;
    
    public IniBoolean(String string){
      super(string);
      if(matchResult.find()){
        value = Boolean.valueOf(matchResult.group());
      }
    }
  
    public static boolean is(String string){
      return format.matcher(string).matches();
    }
    
    @Override
    public Boolean get(){
      return value;
    }
  
    @Override
    public Class<?> type(){
      return Boolean.class;
    }
  
    @Override
    public Pattern formatPattern(){
      return format;
    }
    
    @Override
    public Pattern matcherPattern(){
      return format;
    }
  
    @Override
    public void write(BufferedWriter writer) throws IOException{
      writer.write(Boolean.toString(value));
    }
  }
  
  public static class IniAnnotation extends IniObject{
    private static final Pattern format = Pattern.compile("^[#;].+");
    
    protected String annotation;
    
    public IniAnnotation(String string){
      super(string);
      if(matchResult.find()){
        annotation = matchResult.group();
      }
    }
  
    public static boolean is(String string){
      return format.matcher(string).matches();
    }
  
    @Override
    public String get(){
      return annotation;
    }
  
    @Override
    public Pattern formatPattern(){
      return format;
    }
    
    @Override
    public Pattern matcherPattern(){
      return format;
    }
  
    @Override
    public void write(BufferedWriter writer) throws IOException{
      writer.write(annotation);
    }
  }
  
}
