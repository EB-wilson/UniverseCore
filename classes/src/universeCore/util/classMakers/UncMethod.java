package universeCore.util.classMakers;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static universeCore.util.classMakers.UncClass.GlobalInvokes;

public class UncMethod<R> extends Component{
  protected Class<R> returnType;
  protected LinkedHashMap<String, Class<?>> paramList = new LinkedHashMap<>();
  
  protected ArrayList<MethodContext> context = new ArrayList<>();
  
  public ArrayList<Class<?>> importRequires = new ArrayList<>();
  
  public UncMethod(String name, Class<R> returnType, Object...param){
    super(name);
    this.returnType = returnType;
    if(returnType != null) importRequires.add(returnType);
    for(int i=0; i<param.length; i+=2){
      Class<?> type = (Class<?>) param[i];
      String paramName = (String) param[i+1];
      paramList.put(paramName, type);
    }
  }
  
  public UncMethod(String name, Class<R> returnType, Class<?>...param){
    super(name);
    this.returnType = returnType;
    if(returnType != null) importRequires.add(returnType);
    for(int i=0; i<param.length; i++){
      Class<?> type = param[i];
      paramList.put("$" + i, type);
    }
  }
  
  public String getParameter(){
    boolean[] first = {true};
    StringBuilder params = new StringBuilder();
    paramList.forEach((name, clazz) -> {
      params.append(first[0]? "": ", ").append(clazz.getName()).append(" ").append(name);
      first[0] = false;
    });
    return params.toString();
  }
  
  public void addContext(MethodContext c){
    context.add(c);
  }
  
  public void addContext(String code){
    context.add(new MethodCode(code));
  }
  
  public void addContext(MethodInvoker invoker){
    context.add(new LambdaInvoker(invoker));
  }
  
  protected static abstract class MethodContext{
    public abstract String getCode();
  }
  
  protected static class MethodCode extends MethodContext{
    public final String code;
    
    public MethodCode(String code){
      this.code = code;
    }
    
    @Override
    public String getCode(){
      return code;
    }
  }
  
  protected class LambdaInvoker extends MethodContext{
    public final MethodInvoker invokeLambda;
    public final int invokerId;
    
    public LambdaInvoker(MethodInvoker methodInvoker){
      int index = GlobalInvokes.indexOf(methodInvoker);
      if(index < 0){
        invokerId = GlobalInvokes.size();
        GlobalInvokes.add(methodInvoker);
      }
      else{
        invokerId = index;
      }
      this.invokeLambda = methodInvoker;
    }
  
    @Override
    public String getCode(){
      boolean[] first = {true};
      StringBuilder params = new StringBuilder();
      paramList.forEach((name, clazz) -> {
        params.append(first[0]? "": ", ").append(name);
        first[0] = false;
      });
      
      return "lambda$Invoker$" + invokerId + ".invoke(" + ((modifiers & Modifier.STATIC) != 0? "null": "this") + ", new Object[]{" + params + "});";
    }
  }
}
