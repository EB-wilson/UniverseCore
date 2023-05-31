public class Test{
  public static void main(final String[] args) throws NoSuchMethodException{
    for(int i = 0; i < 20; i++){
      System.out.println("origin: " + i + ", result: " + test(i));
    }
  }

  public static int test(int v){
    return (v & 0xf);
  }
}
