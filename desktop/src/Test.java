
import universeCore.util.handler.EnumHandler;
import universeCore.util.handler.FieldHandler;

import java.lang.reflect.Field;

public class Test{
  public static void main(String[] args){
    Test1 test1 = new Test1();
    FieldHandler.setValue(Test1.class, "a", test1, 10);
    System.out.println(test1.a);
  }
  
  static class Stick{
    public int a;
  }
  
  static class Test1 extends Stick{}
}
