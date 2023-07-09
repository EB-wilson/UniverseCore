package universecore.desktop9core.classes;

import dynamilize.Demodulator;
import universecore.desktopcore.classes.DesktopDynamicClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class DesktopDynamicClassLoader9 extends DesktopDynamicClassLoader {
  private static final Object unsafe;
  private static final Method defineClass;

  static{
    try{
      Demodulator.makeModuleOpen(Object.class.getModule(), "jdk.internal.misc", DesktopDynamicClassLoader9.class.getModule());

      Class<?> clazz = Class.forName("jdk.internal.misc.Unsafe");

      defineClass = clazz.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);

      Constructor<?> cstr = clazz.getDeclaredConstructor();
      cstr.setAccessible(true);
      unsafe = cstr.newInstance();
    }catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException |
           ClassNotFoundException e){
      throw new RuntimeException(e);
    }
  }

  public DesktopDynamicClassLoader9(ClassLoader parent) {
    super(parent);
  }

  @Override
  protected Class<?> defineClass(String name, byte[] bytes, Class<?> accessor){
    try {
      return (Class<?>) defineClass.invoke(unsafe, name, bytes, 0, bytes.length, accessor.getClassLoader(), null);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
