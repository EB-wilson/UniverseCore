import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Main{
  static long timer, result;
  
  public static void main(String[] args){
    int b = 0;
    String s = "null";
    
    Test test = new Test();
    
    //全使用try-catch块减少干扰变量
    try{
      System.out.println("字段访问纳秒测试: 常规访问");
      timer = System.nanoTime();
      b = (int) test.a;
      s = (String) test.str;
  
      result = System.nanoTime() - timer;
      System.out.println("测试结果:" + result + "ns");
      System.out.println(b);
      System.out.println(s);
    }
    catch(Throwable e){
      e.printStackTrace();
    }
    
    b = 0;
    s = "null";
    
    try{
      System.out.println("字段访问纳秒测试: 反射访问");
      //获取字段置于外部
      Field aF = Test.class.getField("a");
      Field strF = Test.class.getField("str");
  
      timer = System.nanoTime();
      b = (int) aF.get(test);
      s = (String) strF.get(test);
  
      result = System.nanoTime() - timer;
      System.out.println("测试结果:" + result + "ns");
      System.out.println(b);
      System.out.println(s);
    }
    catch(Throwable e){
      e.printStackTrace();
    }
    
    try{
      System.out.println("方法访问纳秒测试: 常规访问");
      
    }
    catch(Throwable e){
      e.printStackTrace();
    }
    
    try{
      System.out.println("方法访问纳秒测试: 反射访问");
      Method method = Test.class.getMethod("run");
  
      for(int i = 0; i < 10000; i++){
        timer = System.nanoTime();
        method.invoke(test);
        result = System.nanoTime() - timer;
        System.out.println("测试结果:" + result + "ns");
  
        timer = System.nanoTime();
        test.run();
        result = System.nanoTime() - timer;
        System.out.println("测试结果:" + result + "");
      }
    }
    catch(Throwable e){
      e.printStackTrace();
    }
  }
  
  public static class Test{
    //排除干扰变量，都需类型转换
    public Object a = 10;
    public Object str = "testing";
    
    public void run(){
      System.out.println("running");
    }
  }
}

