

public class Test{
  Object b;
  
  public boolean run(){
    return (boolean) b;
  }
  public static void main(String[] args){
    System.out.println(Test.class.getMethods()[1].getReturnType());
  }
}
