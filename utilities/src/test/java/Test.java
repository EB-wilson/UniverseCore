public class Test{
  public static int i;

  public static void main(String[] args) throws Throwable{
    System.out.println(args[0] + args[1]);
    System.out.println(args[0] + i);
    System.out.println(i + args[1]);
  }
}
