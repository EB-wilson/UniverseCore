package universecore;

import universecore.annotations.Annotations;

import java.util.ArrayList;

public class Test{
  public static void main(String[] args){

  }

  interface T{
    @Annotations.MethodEntry(entryMethod = "doit", context = "data -> i", insert = Annotations.InsertPosition.HEAD)
    default void run(float i){}

    @Annotations.BindField(value = "t", initialize = "new  java.util.ArrayList<>()")
    default ArrayList<?> get(){
      return null;
    }
  }

  @Annotations.ImplEntries
  static interface N extends T{
  }

  static class Test1 implements N{
    public void run(){
      N.super.run(19);
    }
  }
}