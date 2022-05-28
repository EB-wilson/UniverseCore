public class Test{
  public static void main(String[] args) throws Throwable{
    Test1 t = new Test1();
  }

  static class Test1{
    public void run1(){
      System.out.println("run1");
    }

    public void run2(String inf){
      System.out.println("run2: " + inf);
    }

    public static void runStatic(String p){
      System.out.println(p);
    }
  }
}
