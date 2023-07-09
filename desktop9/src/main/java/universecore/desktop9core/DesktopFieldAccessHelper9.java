package universecore.desktop9core;

import dynamilize.Demodulator;
import universecore.desktopcore.DesktopFieldAccessHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class DesktopFieldAccessHelper9 extends DesktopFieldAccessHelper{
  private static final Field modifiers;

  static {
    try {
      Demodulator.makeModuleOpen(
              Field.class.getModule(),
              Field.class.getPackage(),
              DesktopFieldAccessHelper9.class.getModule()
      );
      modifiers = Field.class.getDeclaredField("modifiers");
      modifiers.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Field getField0(Class<?> clazz, String field) throws NoSuchFieldException{
    Field res = clazz.getDeclaredField(field);
    Demodulator.makeModuleOpen(
        clazz.getModule(),
        clazz,
        DesktopFieldAccessHelper9.class.getModule()
    );
    res.setAccessible(true);

    if((res.getModifiers() & Modifier.FINAL) != 0){
      try{
        modifiers.set(res, res.getModifiers() & ~Modifier.FINAL);
      }catch(Throwable e){
        throw new RuntimeException(e);
      }
    }

    return res;
  }
}
