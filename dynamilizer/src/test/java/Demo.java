public class Demo{
  Object[][][] arr;

  static int i;

  Class<?> typ = Demo.class;
  public double set(){
    double a = i;
    double b = a + i;

    return a + b;
  }
}

class Main{
  public static void main(String[] args){
    new Demo();
  }
}

interface AnnoTest{
  long[] value();
}
