package universeCore.util.classMakers;

import arc.struct.ObjectSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UncClass<T> extends Component{
  public static final ArrayList<MethodInvoker> GlobalInvokes = new ArrayList<>();
  
  protected final HashMap<MethodInvoker, Integer> invokers = new HashMap<>();
  
  protected Class<T> clazz;
  
  public Class<? super T> parentClass;
  public ArrayList<Class<?>> interfaces = new ArrayList<>();
  public ArrayList<Component> elements = new ArrayList<>();
  
  public final String simpleName;
  public final String packageName;
  
  public UncClass(String name){
    super(name);
    String[] pac = name.split("\\.");
    
    simpleName = pac[pac.length-1];
    StringBuilder path = new StringBuilder(pac[0]);
    for(int i=1; i<pac.length-1; i++){
      path.append(".");
      path.append(pac[i]);
    }
    
    packageName = path.toString();
  }
  
  public void declareMethod(UncMethod<?> method){
    for(UncMethod.MethodContext context: method.context){
      if(context instanceof UncMethod.LambdaInvoker){
        invokers.put(((UncMethod<?>.LambdaInvoker) context).invokeLambda, ((UncMethod<?>.LambdaInvoker) context).invokerId);
      }
    }
  }
}
