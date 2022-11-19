import java.util.Arrays;
import java.util.function.Consumer;

public class Demo{

  public static void main(String[] args) throws NoSuchFieldException{
    int a = 10;
    String s = "";

    Consumer<Object> c = e -> {
      System.out.println(a + s);
    };
    System.out.println(Arrays.toString(c.getClass().getDeclaredFields()));
  }
}
