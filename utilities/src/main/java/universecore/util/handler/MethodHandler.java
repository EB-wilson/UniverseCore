package universecore.util.handler;

import universecore.ImpCore;

import java.util.HashMap;

@SuppressWarnings("unchecked")
public class MethodHandler<T>{
  private static final String CONSTRUCTOR = "<init>";

  private static final HashMap<Class<?>, MethodHandler<?>> defaultMap = new HashMap<>();

  private final Class<T> clazz;

  public MethodHandler(Class<T> clazz){
    this.clazz = clazz;
  }

  public <R> R invoke(T object, String name, Object... args){
    return ImpCore.methodInvokeHelper.invoke(object, name, args);
  }

  public <R> R invokeStatic(String name, Object... args){
    return ImpCore.methodInvokeHelper.invokeStatic(clazz, name, args);
  }

  public T newInstance(Object... args){
    return ImpCore.methodInvokeHelper.newInstance(clazz, args);
  }

  public static <Obj, Return> Return invokeDefault(Obj object, String name, Object... args){
    return ((MethodHandler<Obj>)defaultMap.computeIfAbsent(object.getClass(), e -> new MethodHandler<>(object.getClass()))).invoke(object, name, args);
  }

  public static <Type, Return> Return invokeDefault(Class<Type> clazz, String name, Object... args){
    return defaultMap.computeIfAbsent(clazz, e -> new MethodHandler<>(clazz)).invokeStatic(name, args);
  }

  public static <Obj, Return> Return invokeTemp(Obj object, String name, Object... args){
    return ((MethodHandler<Obj>) new MethodHandler<>(object.getClass())).invoke(object, name, args);
  }

  public static <Type, Return> Return invokeTemp(Class<Type> clazz, String name, Object... args){
    return new MethodHandler<>(clazz).invokeStatic(name, args);
  }

  public static <Type> Type newInstanceDefault(Class<Type> clazz, Object... args){
    return (Type) defaultMap.computeIfAbsent(clazz, e -> new MethodHandler<>(clazz)).newInstance(args);
  }

  public static <Type> Type newInstanceTemp(Class<Type> clazz, Object... args){
    return new MethodHandler<>(clazz).newInstance(args);
  }
}
