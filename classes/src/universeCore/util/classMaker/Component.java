package universeCore.util.classMaker;

import javassist.ClassPool;
import javassist.CtClass;

public abstract class Component{
  protected static final ClassPool classPool = ClassPool.getDefault();
  
  static{
    classPool.getClassLoader();
  }
  
  public final String name;
  protected int modifiers;
  
  protected Component(String name){
    this.name = name;
  }
  
  public int getModifiers(){
    return modifiers;
  }
  
  public void setModifiers(int mode){
    modifiers = mode;
  }
  
  public void addModifiers(int mode){
    modifiers = modifiers | mode;
  }
  
  public void removeModifiers(int mode){
    modifiers = modifiers & ~mode;
  }
  
  public abstract void handle(CtClass making, UncClass clazz);
}
