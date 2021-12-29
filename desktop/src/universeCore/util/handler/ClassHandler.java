package universeCore.util.handler;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import universeCore.util.classMakers.IClassHandler;
import universeCore.util.classMakers.UncClass;
import universeCore.util.classMakers.UncField;
import universeCore.util.classMakers.UncMethod;

public class ClassHandler implements IClassHandler{
  private static final ClassPool classPool = ClassPool.getDefault();
  
  private CtClass currClass;
  
  @Override
  public Class<?> toClass(UncClass<?> clazz){
    currClass = classPool.makeClass(clazz.name);
    try{
      currClass.setSuperclass(classPool.get(clazz.parentClass.getName()));
      CtClass[] interfaces = new CtClass[clazz.interfaces.size()];
      for(int i=0; i<interfaces.length; i++){
        interfaces[i] = classPool.get(clazz.interfaces.get(i).getName());
      }
      currClass.setInterfaces(interfaces);
      currClass.setModifiers(clazz.modifiers);
      return currClass.toClass();
    }catch(CannotCompileException | NotFoundException e){
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void handleMethod(UncMethod<?> method){
  
  }
  
  @Override
  public void handleField(UncField<?> field){
  
  }
}
