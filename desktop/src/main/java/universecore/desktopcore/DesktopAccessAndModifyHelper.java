package universecore.desktopcore;

import sun.misc.Unsafe;
import universecore.util.AccessAndModifyHelper;

import java.lang.reflect.*;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked"})
public class DesktopAccessAndModifyHelper implements AccessAndModifyHelper{
  private static final long fieldFilterOffset = 112L;

  private static final Unsafe unsafe;

  private static Field fieldModifiers;

  static{
    try{
      Constructor<Unsafe> cstr = Unsafe.class.getDeclaredConstructor();
      cstr.setAccessible(true);
      unsafe = cstr.newInstance();

      Class<?> clazz = Class.forName("jdk.internal.reflect.Reflection");
      Map<Class<?>, Set<String>> map = (Map<Class<?>, Set<String>>) unsafe.getObject(clazz, fieldFilterOffset);
      map.clear();
    }catch(NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException|ClassNotFoundException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void set(Object object, Field field, Object value){
    try{
      setFieldNonFinal(field);
      field.set(object, value);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setStatic(Class<?> clazz, Field field, Object value){
    try{
      setFieldNonFinal(field);
      field.set(null, value);
    }catch(IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setAccessible(Field field){
    Class<?> clazz = field.getDeclaringClass();
    Demodulator.checkAndMakeModuleOpen(clazz.getModule(), clazz.getPackage(), getClass().getModule());
    field.setAccessible(true);
  }

  @Override
  public void setAccessible(Method method){
    Class<?> clazz = method.getDeclaringClass();
    Demodulator.checkAndMakeModuleOpen(clazz.getModule(), clazz.getPackage(), getClass().getModule());
    method.setAccessible(true);
  }

  @Override
  public <T> void setAccessible(Constructor<T> cstr){
    Class<?> clazz = cstr.getDeclaringClass();
    Demodulator.checkAndMakeModuleOpen(clazz.getModule(), clazz.getPackage(), getClass().getModule());
    cstr.setAccessible(true);
  }

  private void setFieldNonFinal(Field field){
    try{
      if(Modifier.isFinal(field.getModifiers())){
        if(fieldModifiers == null){
          fieldModifiers = Field.class.getDeclaredField("modifiers");
          Demodulator.checkAndMakeModuleOpen(Field.class.getModule(), Field.class.getPackage(), getClass().getModule());
          fieldModifiers.setAccessible(true);
        }
        fieldModifiers.set(field, field.getModifiers() & ~Modifier.FINAL);
      }
      field.setAccessible(true);
    }catch(NoSuchFieldException|IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }
}
