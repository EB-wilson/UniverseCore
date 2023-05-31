import java.util.HashMap;

public class TimeTest{
  public static void main(String[] args){
    HashMap<String, Integer> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", 2);
    map.put("c", 3);
    map.put("d", 4);
    map.put("e", 5);

    long l = System.nanoTime();
    for(int i = 0; i< 1024; i++){
      map.get("a");
    }
    l = System.nanoTime() - l;
    System.out.println(l/1024);
  }

  public static void run(){}
}
