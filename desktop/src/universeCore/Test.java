package universeCore;

import universeCore.util.proxy.BaseProxy;
import universeCore.util.proxy.DesktopProxy;

import java.util.Arrays;

public class Test{
  public static void main(String[] args){
    BaseProxy<Test1> proxy = new DesktopProxy<>(Test1.class);
    try{
      proxy.addMethodProxy(Test1.class.getMethod("run", boolean.class, String.class), (s, su, p) -> {
        su.callSuper(s, p);
        System.out.println("im running" + Arrays.toString(p));
        return false;
      });
      Test1 t = proxy.create(null);
      System.out.println(t.run(true, "run"));
    }catch(NoSuchMethodException e){
      e.printStackTrace();
    }
  }
  
  public static class Test1{
    public boolean run(boolean b, String str){
      System.out.println(str);
      return true;
    }
  }
}
