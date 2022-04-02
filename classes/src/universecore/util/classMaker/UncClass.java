package universecore.util.classMaker;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

public class UncClass extends Component{
  public static final ArrayList<MethodInvoker> GlobalInvokes = new ArrayList<>();
  
  private static final Pattern innerMatcher = Pattern.compile("this.\\$\\d+\\$*");
  
  protected final HashSet<MethodInvoker> invokers = new HashSet<>();
  
  protected Class<?> closedClass;
  protected boolean isInnerClass;
  
  protected Class<?> superClass;
  protected ArrayList<UncConstructor> constructors = new ArrayList<>();
  protected ArrayList<Class<?>> interfaces = new ArrayList<>();
  protected ArrayList<UncField<?>> fields = new ArrayList<>();
  protected ArrayList<UncMethod> methods = new ArrayList<>();
  
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
  
  @Override
  public void handle(CtClass making, UncClass clazz){}
  
  public byte[] getByteCode(){
    CtClass currClass = classPool.makeClass(name);
    try{
      if(superClass != null)currClass.setSuperclass(classPool.get(superClass.getName()));
      CtClass[] interfaces = new CtClass[getInterfaces().size()];
      for(int i=0; i<interfaces.length; i++){
        interfaces[i] = classPool.get(getInterfaces().get(i).getName());
      }
      currClass.setInterfaces(interfaces);
      currClass.setModifiers(getModifiers());
      
      ArrayList<Component> elements = new ArrayList<>();
      elements.addAll(getFields());
      elements.addAll(getConstructors());
      elements.addAll(getMethods());
  
      for(Component element : elements){
        element.handle(currClass, this);
      }
      return currClass.toBytecode();
    }catch(CannotCompileException | NotFoundException | IOException e){
      throw new RuntimeException(e);
    }
  }
  
  public void setSuperClass(Class<?> clazz){
    superClass = clazz;
    Class<?> getting = clazz;
    while(getting != Object.class){
      for(Field field : getting.getDeclaredFields()){
        if(innerMatcher.matcher(field.getName()).matches()){
          isInnerClass = true;
          closedClass = field.getClass();
          break;
        }
      }
      
      getting = clazz.getSuperclass();
    }
  }
  
  public boolean isInnerClass(){
    return isInnerClass;
  }
  
  public Class<?> getClosedClass(){
    return closedClass;
  }
  
  public Class<?> getSuperClass(){
    return superClass;
  }
  
  public ArrayList<Class<?>> getInterfaces(){
    return interfaces;
  }
  
  public ArrayList<UncConstructor> getConstructors(){
    return constructors;
  }
  
  public ArrayList<UncField<?>> getFields(){
    return fields;
  }
  
  public ArrayList<UncMethod> getMethods(){
    return methods;
  }
  
  public void declareField(UncField<?> field){
    fields.add(field);
  }
  
  public void declareMethod(UncMethod method){
    methods.add(method);
    importLambda(method);
  }
  
  public void declareConstructor(UncConstructor method){
    constructors.add(method);
    importLambda(method);
  }
  
  private void importLambda(UncMethod method){
    for(UncMethod.MethodContext context: method.context){
      if(context instanceof UncMethod.LambdaInvoker){
        if(invokers.add(((UncMethod.LambdaInvoker) context).invokeLambda)){
          UncField<MethodInvoker> invoker = new UncField<>(MethodInvoker.class, "lambdaInvoker" + ((UncMethod.LambdaInvoker) context).invokerId,
              "universeCore.util.classMakers.UncClass.GlobalInvokes.get(" + ((UncMethod.LambdaInvoker) context).invokerId + ")");
          invoker.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
          fields.add(0, invoker);
        }
      }
    }
  }
}
