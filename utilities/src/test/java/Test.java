import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class Test{
  public static int i;

  public static void main(String[] args) throws Throwable{
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    Method met = Test.class.getMethod("run", long.class, String.class);

    MethodHandle method = lookup.unreflect(met);
    Test inst = new Test();
    Object[] arg = new Object[]{System.nanoTime(), "EBwilson"};
    method.invoke(inst, arg);
  }

  public void run(long time, String name){
    System.out.println(name + " " + time);
  }
}
