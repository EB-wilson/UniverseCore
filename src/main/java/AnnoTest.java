import universecore.annotations.Annotations;

public class AnnoTest{
  public static void main(String[] args){

  }

  interface A{
    @Annotations.BindField("ju")
    default float get(){
      return 0;
    }
  }

  @Annotations.ImplEntries
  public static class T implements A{

  }
}
