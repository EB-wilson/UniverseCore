package universeCore.util.classMaker;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;

import static universeCore.util.classMaker.UncClass.GlobalInvokes;

public class UncMethod extends Component{
  public Class<?> returnType;
  public ArrayList<Class<?>> paramList = new ArrayList<>();
  
  public ArrayList<MethodContext> context = new ArrayList<>();
  
  public ArrayList<Class<?>> importRequires = new ArrayList<>();
  
  public UncMethod(String name, Class<?> returnType, Class<?>...param){
    super(name);
    this.returnType = returnType;
    if(returnType != null) importRequires.add(returnType);
    Collections.addAll(paramList, param);
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
  
  public String getCode(){
    StringBuilder result = new StringBuilder();
    for(MethodContext context: context){
      result.append(context.getCode());
    }
    return result.toString();
  }
  
  @Override
  public void handle(CtClass making, UncClass clazz){
    try{
      CtClass[] param = new CtClass[paramList.size()];
      for(int i = 0; i < param.length; i++){
        param[i] = classPool.get(paramList.get(i).getName());
      }
      CtMethod ctMethod = new CtMethod(returnType == null? CtClass.voidType: classPool.get(returnType.getName()), name, param, making);
      ctMethod.setModifiers(getModifiers());
      ctMethod.setBody("{" + getCode() + "}");
      making.addMethod(ctMethod);
    }catch(NotFoundException | CannotCompileException e){
      e.printStackTrace();
    }
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
      return "$0.lambdaInvoker" + invokerId + ".invoke(" + ((modifiers & Modifier.STATIC) != 0? "null": "this") + ", $args);";
    }
  }
}
