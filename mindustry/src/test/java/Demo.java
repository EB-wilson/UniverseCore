public class Demo extends A{
  public void main(String[] args) {
    super.t();
  }

}

class A implements I{}

interface I{
  default void t(){}
}