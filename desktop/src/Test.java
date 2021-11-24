
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Test{
  private final static Unsafe unsafe;
  
  static{
    Unsafe temp;
    try{
      Constructor<Unsafe> cstr = Unsafe.class.getDeclaredConstructor();
      cstr.setAccessible(true);
      temp = cstr.newInstance();
    }catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e){
      throw new RuntimeException(e);
    }
    unsafe = temp;
  }
  
  public static void main(String[] args){
    Stick a = new Stick(){
      Stick b = new Stick(){
        float c;
      };
  
      @Override
      public Object get(){
        return b;
      }
    };
  
    Class<?> clazz = a.get().getClass();
    while(clazz.getName().contains("$")){
      System.out.println("run: " + clazz.getName());
      clazz = clazz.getSuperclass();
    }
    
    System.out.println(clazz.getName());
  }
  
  public static class Stick{
    public double a;
    
    public double a(){
      return a;
    }
    
    public Object get(){
      return null;
    }
  }
}

