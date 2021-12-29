package universeCore.util.classMakers;

public abstract class Component{
  public final String name;
  public int modifiers;
  
  protected Component(String name){
    this.name = name;
  }
}
