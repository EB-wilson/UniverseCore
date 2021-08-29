package universeCore;


import mindustry.gen.Building;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;

public class Test{
  public static void main(String[] args){
    Enhancer en = new Enhancer();
    en.setSuperclass(Test1.class);
    en.setCallback(new MethodInterceptor(){
      @Override
      public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable{
        return null;
      }
    });
    
    Test1 t = (Test1) en.create(new Class[]{String.class, int.class}, new Object[]{"d", 4});
    System.out.println(t.name());
    
  }
  
  interface T1{
    void run1();
    void run3();
  }
  
  enum Test1{
    a, b,ma, c
  }
}
