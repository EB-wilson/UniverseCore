package universecore;

import universecore.annotations.Annotations;

public class Test{
  public static void main(String[] args){

  }

  interface T{
    @Annotations.MethodEntry(entryMethod = "doit", context = "data -> i", insert = Annotations.InsertPosition.HEAD)
    default void run(float i){}
  }

  @Annotations.ImplEntries
  static class N implements T{
    float data;

    public N(String name){
      System.out.println();
    }

    public String doit(){
      return String.valueOf(898);
    }
  }
}