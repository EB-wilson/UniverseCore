package universeCore.util.classMakers;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

public class UncConstructor extends UncMethod{
  public UncConstructor(Class<?>... param){
    super("<init>", null, param);
  }
  
  @Override
  public void handle(CtClass making, UncClass clazz){
    try{
      CtClass[] param;
      int i;
      if(clazz.isInnerClass()){
        param = new CtClass[paramList.size() + 1];
        param[0] = classPool.get(clazz.getClosedClass().getName());
        i = 1;
      }
      else{
        param = new CtClass[paramList.size()];
        i = 0;
      }
    
      for(; i < param.length; i++){
        param[i] = classPool.get(paramList.get(i).getName());
      }
      CtConstructor cstr = new CtConstructor(param, making);
      cstr.setModifiers(getModifiers());
      cstr.setBody("{" + getCode() + "}");
      making.addConstructor(cstr);
    }catch(NotFoundException | CannotCompileException e){
      e.printStackTrace();
    }
  }
}
