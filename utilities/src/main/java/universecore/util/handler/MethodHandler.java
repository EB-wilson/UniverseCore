package universecore.util.handler;

import universecore.ImpCore;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class MethodHandler<T>{
  private static final String CONSTRUCTOR = "<init>";

  private static final HashMap<Class<?>, MethodHandler<?>> defaultMap = new HashMap<>();

  private final Class<T> clazz;
  private final HashMap<String, HashMap<MethodType, MethodHandle>> methods = new HashMap<>();

  private final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public MethodHandler(Class<T> clazz){
    this.clazz = clazz;
  }

  public static Method getDeclaredMethodSuper(Class<?> clazz, String name, Class<?>[] argTypes) throws NoSuchMethodException{
    Class<?> curr = clazz;

    while(curr != null){
      Method res = null;

      try{
        res = curr.getDeclaredMethod(name, argTypes);
      }catch(Throwable ignored){}

      if(res != null) return res;

      curr = curr.getSuperclass();
    }

    curr = clazz;
    while(curr != null){
      tag: for(Method method: curr.getDeclaredMethods()){
        if(!method.getName().equals(name)) continue;
        Class<?>[] methodArgs = method.getParameterTypes();

        if(argTypes.length != methodArgs.length) continue;

        for(int i = 0; i < argTypes.length; i++){
          if(argTypes[i] == void.class) continue;
          if(!methodArgs[i].isAssignableFrom(argTypes[i])) continue tag;
        }

        return method;
      }

      curr = curr.getSuperclass();
    }

    throw new NoSuchMethodException("no such method " + name + " in class: " + clazz + " with assignable parameter: " + Arrays.toString(argTypes));
  }

  public static <T> Constructor<T> getDeclaredConstructorAssign(Class<T> clazz, Class<?>[] argTypes) throws NoSuchMethodException{
    tag: for(Constructor<?> constructor: clazz.getDeclaredConstructors()){
      Class<?>[] methodArgs = constructor.getParameterTypes();

      if(argTypes.length != methodArgs.length) continue;

      for(int i = 0; i < argTypes.length; i++){
        if(!methodArgs[i].isAssignableFrom(argTypes[i])) continue tag;
      }

      return (Constructor<T>) constructor;
    }

    throw new NoSuchMethodException("no such constructor in class: " + clazz + " with assignable parameter: " + Arrays.toString(argTypes));
  }

  public <R> R invoke(T object, String name, Object... args){
    Class<?>[] argTypes = new Class[args.length];

    for(int i = 0; i < args.length; i++){
      argTypes[i] = asBasic(args[i] == null? void.class: args[i].getClass());
    }

    try{
      Method method = getDeclaredMethodSuper(clazz, name, argTypes);

      MethodType type = MethodType.methodType(method.getReturnType(), method.getParameterTypes()).unwrap();
      MethodHandle handle = methods.computeIfAbsent(name, e -> new HashMap<>()).computeIfAbsent(
          type,
          e -> {
            try{
              ImpCore.accessAndModifyHelper.setAccessible(method);
              return lookup.unreflect(method);
            }catch(IllegalAccessException ex){
              throw new RuntimeException(ex);
            }
          }
      );

      if(object != null){
        handle = handle.bindTo(object);
      }

      return (R) handle.invokeWithArguments(args);
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  public T newInstance(Object... args){
    Class<?>[] argTypes = new Class[args.length];

    for(int i = 0; i < args.length; i++){
      argTypes[i] = asBasic(args[i] == null? void.class: args[i].getClass());
    }

    try{
      Constructor<T> cstr = getDeclaredConstructorAssign(clazz, argTypes);
      MethodType type = MethodType.methodType(clazz, argTypes).unwrap();

      MethodHandle handle = methods.computeIfAbsent(CONSTRUCTOR, e -> new HashMap<>()).computeIfAbsent(
          type,
          e -> {
            try{
              ImpCore.accessAndModifyHelper.setAccessible(cstr);
              return lookup.unreflectConstructor(cstr);
            }catch(IllegalAccessException ex){
              throw new RuntimeException(ex);
            }
          }
      );

      return (T) handle.invokeWithArguments(args);
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  public static <Obj, Return> Return invokeDefault(Obj object, String name, Object... args){
    return ((MethodHandler<Obj>)defaultMap.computeIfAbsent(object.getClass(), e -> new MethodHandler<>(object.getClass()))).invoke(object, name, args);
  }

  public static <Type, Return> Return invokeDefault(Class<Type> clazz, String name, Object... args){
    return defaultMap.computeIfAbsent(clazz, e -> new MethodHandler<>(clazz)).invoke(null, name, args);
  }

  public static <Obj, Return> Return invokeTemp(Obj object, String name, Object... args){
    return ((MethodHandler<Obj>) new MethodHandler<>(object.getClass())).invoke(object, name, args);
  }

  public static <Type, Return> Return invokeTemp(Class<Type> clazz, String name, Object... args){
    return new MethodHandler<>(clazz).invoke(null, name, args);
  }

  public static <Type> Type newInstanceDefault(Class<Type> clazz, Object... args){
    return (Type) defaultMap.computeIfAbsent(clazz, e -> new MethodHandler<>(clazz)).newInstance(args);
  }

  public static <Type> Type newInstanceTemp(Class<Type> clazz, Object... args){
    return new MethodHandler<>(clazz).newInstance(args);
  }

  private static Class<?> asBasic(Class<?> clazz){
    if(clazz == Integer.class) return int.class;
    if(clazz == Float.class) return float.class;
    if(clazz == Double.class) return double.class;
    if(clazz == Long.class) return long.class;
    if(clazz == Boolean.class) return boolean.class;
    if(clazz == Short.class) return short.class;
    if(clazz == Byte.class) return byte.class;
    return clazz;
  }
}
