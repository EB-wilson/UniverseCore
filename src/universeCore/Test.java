package universeCore;

public class Test{
  public boolean boo = true;
  public static void main(String[] args){
  
    try{
      System.out.println(Test.class.getField("boo").getType());
    }catch(NoSuchFieldException e){
      e.printStackTrace();
    }
  }
}
