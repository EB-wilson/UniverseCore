package dynamilize;

public class IllegalHandleException extends RuntimeException{
  public IllegalHandleException(){
  }

  public IllegalHandleException(String info){
    super(info);
  }

  public IllegalHandleException(Throwable e){
    super(e);
  }

  public IllegalHandleException(String info, Throwable caused){
    super(info, caused);
  }
}
