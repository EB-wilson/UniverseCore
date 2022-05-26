package universecore.desktopcore;

import sun.misc.Unsafe;
import universecore.util.FinalSetter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked"})
public class DesktopFinalSetter implements FinalSetter{
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

  private void setFieldNonFinal(Field field){
    try{
      if((field.getModifiers()&Modifier.FINAL) != 0){
        if(fieldModifiers == null){
          fieldModifiers = Field.class.getDeclaredField("modifiers");
          Demodulator.checkAndMakeModuleOpen(Field.class.getModule(), Field.class.getPackage(), getClass().getModule());
          fieldModifiers.setAccessible(true);
        }
        fieldModifiers.set(field, fieldModifiers.getModifiers() & ~Modifier.FINAL);
      }
      field.setAccessible(true);
    }catch(NoSuchFieldException|IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }
}
