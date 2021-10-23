
import universeCore.util.handler.EnumHandler;
import universeCore.util.handler.FieldHandler;

public class Test{
  public static void main(String[] args){
    Stick current = new Stick("first");
    for(int i = 0; i<=15; i++){
      current = new Stick(current, "" + i);
      
      if(i % 5 == 0 && i > 0){
        Stick reflow = current;
        while(!reflow.first){
          System.out.println(reflow.self);
          reflow = reflow.previous;
        }System.out.println(reflow.self);
      }
    }
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
