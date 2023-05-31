package universecore.desktop9core;

import universecore.desktopcore.DesktopFieldAccessHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings("unchecked")
public class DesktopFieldAccessHelper9 extends DesktopFieldAccessHelper{
  private static final boolean useUnsafe;

  static {
    boolean tmp;
    try{
      Class.forName("sun.misc.Unsafe");
      tmp = true;
    }catch(ClassNotFoundException e){
      tmp = false;
    }
    useUnsafe = tmp;
  }

  @Override
  protected Field getField0(Class<?> clazz, String field) throws NoSuchFieldException{
    Field res = clazz.getDeclaredField(field);
    Demodulator.checkAndMakeModuleOpen(
        clazz.getModule(),
        clazz,
        DesktopFieldAccessHelper9.class.getModule()
    );
    res.setAccessible(true);

    if((res.getModifiers() & Modifier.FINAL) != 0){
      try{
        Field modifiers = Field.class.getDeclaredField("modifiers");
        Demodulator.checkAndMakeModuleOpen(
            Field.class.getModule(),
            Field.class.getPackage(),
            DesktopFieldAccessHelper9.class.getModule()
        );
        modifiers.setAccessible(true);
        modifiers.set(res, res.getModifiers() & ~Modifier.FINAL);
      }catch(Throwable e){
        throw new RuntimeException(e);
      }
    }

    return res;
  }

  @Override
  public void set(Object object, String field, Object value){
    try{
      if(useUnsafe){
        Field f = getField(object.getClass(), field, false);
        UnsafeAccess.set(object, f, value);
      }
      else{
        super.set(object, field, value);
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setStatic(Class<?> clazz, String field, Object value){
    try{
      if(useUnsafe){
        Field f = getField(clazz, field, false);
        UnsafeAccess.setStatic(f, value);
      }
      else{
        super.setStatic(clazz, field, value);
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T get(Object object, String field){
    try{
      if(useUnsafe){
        Field f = getField(object.getClass(), field, false);
        return (T) UnsafeAccess.get(object, f);
      }
      else{
        return super.get(object, field);
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T getStatic(Class<?> clazz, String field){
    try{
      if(useUnsafe){
        Field f = getField(clazz, field, true);
        return (T) UnsafeAccess.getStatic(f);
      }
      else{
        return super.getStatic(clazz, field);
      }
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }
}
