package testbuild;

import universecore.annotations.Annotations;

@Annotations.ImplEntries
public class Test extends A implements I{
  @Override
  public void subTrigger(String arg) {
    I.super.subTrigger(arg);
    System.out.println(arg);
  }
}

class A extends S{
  public String transToLow(String in){
    return in;
  }
}

class S{
  public String transToUp(String in){
    return in;
  }

  public void run(String in){}

  public int doing(){
    return 1;
  }
}
