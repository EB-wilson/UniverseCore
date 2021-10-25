
import universeCore.util.handler.EnumHandler;
import universeCore.util.handler.FieldHandler;

public class Test{
  public static void main(String[] args){
    Object i = 2;
    System.out.println(i.getClass());
  }
  
  static class Stick{
    Stick previous;
    String self;
    boolean first;
  
    Stick(Stick pre, String self){
      previous = pre;
      this.self = self;
      first = false;
    }
  
    Stick(String self){
      previous = null;
      this.self = self;
      first = true;
    }
  }
}
