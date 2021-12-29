import javassist.*;
import universeCore.util.classMakers.Component;
import universeCore.util.classMakers.UncClass;
import universeCore.util.classMakers.UncMethod;
import universeCore.util.handler.ClassHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Main{
  public static void main(String[] args) throws ClassNotFoundException{
    ClassPool pool = ClassPool.getDefault();
    try{
      long time = System.nanoTime();
      CtClass ctClass = pool.makeClass("Test2");
      ctClass.setSuperclass(pool.get(Test.Test1.class.getName()));
      CtConstructor cstr = new CtConstructor(new CtClass[]{pool.get(Test.class.getName())}, ctClass);
      cstr.setBody("{super($1);}");
      ctClass.addConstructor(cstr);
      CtMethod method = new CtMethod(CtClass.voidType, "get", new CtClass[]{}, ctClass);
      method.setModifiers(Modifier.PUBLIC);
      method.setBody("{super.get();System.out.println(\"running\");}");
      ctClass.addMethod(method);
      
      Class<?> clazz = ctClass.toClass();
      System.out.println(System.nanoTime() - time);
      
      Test t = new Test();
      Test.Test1 o = (Test.Test1) clazz.getConstructor(Test.class).newInstance(t);
      o.get();
    }catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | CannotCompileException | NotFoundException e){
      e.printStackTrace();
    }
  }
}

class Test{
  int i=10;
  public class Test1{
    public void get(){
      System.out.println(i);
    }
  }
}

