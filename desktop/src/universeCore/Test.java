package universeCore;

import javassist.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Test{
  public static void main(String[] args){
    ClassPool pool = ClassPool.getDefault();
  
    try{
      CtClass clazz = pool.makeClass("Testing");
      clazz.setModifiers(Modifier.PUBLIC);
  
      CtField f = new CtField(pool.get(Test.class.getName()), "field", clazz);
      f.setModifiers(Modifier.PUBLIC | Modifier.STATIC);
      clazz.addField(f, "new universeCore.Test()");
      
      CtMethod m = new CtMethod(CtClass.voidType, "test", new CtClass[]{}, clazz);
      m.setModifiers(Modifier.PUBLIC);
      m.setBody("{field.run();}");
      clazz.addMethod(m);
      
      Class<?> c = clazz.toClass();
      Method met = c.getMethod("test");
      
      met.invoke(c.getConstructor().newInstance());
    }catch(NotFoundException | CannotCompileException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e){
      e.printStackTrace();
    }
  }
  
  public void run(){
    System.out.println("hello world");
  }
}
