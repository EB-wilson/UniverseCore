package universeCore;

import universeCore.annotations.Annotations;

interface I{
  @Annotations.BindField("fz")
  default float test(){
    return 0;
  }
  
  @Annotations.BindField("fz")
  default void testFoo2(float f){}
  
  void run();
}

interface Ca extends I{
  @Annotations.MethodEntry(entryMethod = "get")
  default void run(){}
}

interface Interface extends I{
  
  
  @Annotations.MethodEntry(entryMethod = "get")
  default void run(){}
}

class V{
  
  public class Test extends V implements Interface{
    @Override
    public void run(){
      Interface.super.run();
    }
  }
  
  @Annotations.ImplEntries
  public class Test1 extends Test implements Ca{
    public void get(){}
  }
}


