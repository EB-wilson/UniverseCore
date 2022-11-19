import java.io.*;

public class Test implements Serializable {
  public int i;

  public static void main(String[] args) throws Throwable{
    ByteArrayOutputStream outA = new ByteArrayOutputStream();
    try(ObjectOutputStream out = new ObjectOutputStream(outA)){
      out.writeObject(new Test());
    }

    BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(outA.toByteArray()));
    StringWriter writer = new StringWriter();
    int i;
    while((i = in.read()) != -1){
      writer.write(i);
    }
    System.out.println(writer);
  }

  public void run(long time, String name){
    System.out.println(name + " " + time);
  }
}
