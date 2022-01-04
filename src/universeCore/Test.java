package universeCore;

import universeCore.util.handler.ClassHandler;
import universeCore.util.proxy.BaseProxy;

import java.lang.reflect.Method;

public class Test{
  public Test(String s){}
  
  public static void main(String[] args){
    BaseProxy<Test> proxy = ClassHandler.getProxy(Test.class);
    
    try{
      proxy.assignConstruct(Test.class.getConstructor(String.class));
      Method m = Test.class.getMethod("doit", String.class);
      proxy.addMethodProxy(m, (self, superHand, param) -> {
        System.out.println("abc");
        superHand.callSuper(self, param[0] = "ass!");
        return "work";
      });
      Test t = proxy.create(null, "a");
      System.out.println(t.doit("running"));
      System.out.println("----------------");
      proxy.addMethodProxy(m, (self, superHandler, param) -> {
        System.out.println("before");
        superHandler.callSuper(self, param);
        System.out.println("after");
        return "working";
      });
  
      t.doit("running");
    }catch(NoSuchMethodException e){
      e.printStackTrace();
    }
  }
  
  public String doit(String str){
    System.out.println(str);
    return "it work!!!";
  }
}
