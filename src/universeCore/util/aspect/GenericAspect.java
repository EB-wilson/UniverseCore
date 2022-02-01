package universeCore.util.aspect;

import arc.struct.Seq;

public class GenericAspect<Element> extends AbstractAspect<Element, Seq<Element>>{
  protected GenericAspect(){
    super(null);
  }
  
  @Override
  public Seq<Element> instance(){
    return children;
  }
  
  @Override
  public boolean filter(Object target){
    return true;
  }
}
