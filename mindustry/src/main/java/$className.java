import universecore.annotations.Annotations;

@Annotations.ImplEntries
public class $className extends C implements T{

}

class C{
  public int result(int in){
    return in*in;
  }
}

interface T{
  @Annotations.MethodEntry(entryMethod = "result", paramTypes = {"int -> in"}, override = true)
  default int res(int in){
    return in*in*in;
  }
}