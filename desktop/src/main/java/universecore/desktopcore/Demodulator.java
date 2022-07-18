package universecore.desktopcore;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**反模块化工具， 仅提供了一个主要方法{@link Demodulator#makeModuleOpen(Module, Package, Module)}用于强制对需要的模块开放模块的软件包。
 * <p>此类行为可能完全打破模块化的访问保护，本身是不安全的，若不是必要情况，请尽量避免使用该类
 * <p><strong>此类仅在JDK9之后可用，避免在更早的版本引用此类的方法，且此类仅在desktop平台可用，安卓平台不可使用此类的任何行为</strong>*/
@SuppressWarnings({"unchecked"})
public class Demodulator{
  private static final long fieldFilterOffset = 112L;

  private static final Unsafe unsafe;

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

  @SuppressWarnings("unchecked")
  public static void makeModuleOpen(Module from, Package pac, Module to){
    if(checkModuleOpen(from, pac, to)) return;

    try{
      Field opensField = Module.class.getDeclaredField("openPackages");
      Map<String, Set<Module>> opensMap = (Map<String, Set<Module>>) unsafe.getObjectVolatile(from, unsafe.objectFieldOffset(opensField));
      if(opensMap == null){
        opensMap = new HashMap<>();
        unsafe.putObjectVolatile(from, unsafe.objectFieldOffset(opensField), opensMap);
      }

      Set<Module> opens = opensMap.computeIfAbsent(pac.getName(), e -> new HashSet<>());
      try{
        opens.add(to);
      }catch(UnsupportedOperationException e){
        ArrayList<Module> lis = new ArrayList<>(opens);
        lis.add(to);
        opensMap.put(pac.getName(), new HashSet<>(lis));
      }
    }catch(NoSuchFieldException e){
      throw new RuntimeException(e);
    }
  }

  public static boolean checkModuleOpen(Module from, Package pac, Module to){
    Objects.requireNonNull(from);
    Objects.requireNonNull(pac);
    Objects.requireNonNull(to);
    
    return from.isOpen(pac.getName(), to);
  }

  public static boolean checkAndMakeModuleOpen(Module from, Package pac, Module to){
    if(!checkModuleOpen(from, pac, to)){
      makeModuleOpen(from, pac, to);
      return false;
    }
    return true;
  }
}
