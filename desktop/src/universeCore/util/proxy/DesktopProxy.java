package universeCore.util.proxy;

import javassist.*;
import universeCore.util.classes.AbstractFileClassLoader;
import universeCore.util.handler.ClassHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**Proxy的桌面实现，除非你确定自己的mod只用于桌面，否则不要直接引用这个类型
 * @author EBwilson
 * @since 1.2*/
public class DesktopProxy<Target> extends BaseProxy<Target>{
  private static ClassPool pool = ClassPool.getDefault();
  private static int proxyCounter = 0;
  
  private static final CtClass containerType;
  private static final CtClass objectType;
  private static final CtClass proxyType;
  private static final CtClass handleType;
  
  static{
    try{
      pool.insertClassPath(new LoaderClassPath(BaseProxy.class.getClassLoader()));
      containerType = pool.get(BaseProxy.class.getName());
      objectType = pool.get(Object.class.getName());
      proxyType = pool.get(ProxyMethod.class.getName());
      handleType = pool.get(ProxyHandler.class.getName());
    }catch(NotFoundException e){
      throw new RuntimeException(e);
    }
  }
  
  private final AbstractFileClassLoader genLoader;
  private final ClassLoader modLoader;
  
  public DesktopProxy(Class<Target> clazz, AbstractFileClassLoader loader, ClassLoader modClassLoader, ClassHandler handler){
    super(clazz, loader, handler);
    modLoader = modClassLoader;
    genLoader = loader;
    pool = assignPool();
  }
  
  private ClassPool assignPool(){
    ClassPool pool = ClassPool.getDefault();
    pool.insertClassPath(new LoaderClassPath(DesktopProxy.class.getClassLoader()));
    pool.insertClassPath(new LoaderClassPath(modLoader));
    return pool;
  }
  
  @Override
  protected <T extends Target> Class<T> generateProxyClass(){
    String className = clazz.getName() + "$Proxy$" + proxyCounter++;
    HashMap<String, byte[]> classes = new HashMap<>();
    try{
      return loadClass(className, clazz);
    }catch(ClassNotFoundException ignored){
      try{
        if(proxyCounter%64 == 0){
          pool = assignPool();
        }
        CtClass proxyClass = pool.makeClass(className);
        proxyClass.setSuperclass(pool.get(clazz.getName()));
        proxyClass.setInterfaces(transfer(IProxied.class));
        proxyClass.setModifiers(Modifier.PUBLIC);
        
        CtField proxyContainer = new CtField(containerType, "proxyContainer", proxyClass);
        proxyContainer.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
        proxyClass.addField(proxyContainer, "null");
  
        CtConstructor c = new CtConstructor(transfer(), proxyClass);
        c.setModifiers(Modifier.PUBLIC);
        c.setBody("super();");
        proxyClass.addConstructor(c);
        for(Constructor<? extends Target> constructor : constructors){
          CtConstructor cstr = new CtConstructor(transfer(constructor.getParameterTypes()), proxyClass);
          cstr.setModifiers(Modifier.PUBLIC);
          cstr.setBody("super($$);");
          proxyClass.addConstructor(cstr);
        }
    
        CtMethod afterHandle = new CtMethod(CtClass.voidType, "afterHandle", new CtClass[0], proxyClass);
        afterHandle.setModifiers(Modifier.PUBLIC);
        StringBuilder afterSrc = new StringBuilder();
        if(superProxy != null) afterSrc.append("super.afterHandle();");
        for(ProxyMethod<?, Target> proxyMethod : proxies.values()){
          Method method = proxyMethod.targetMethod();
          Class<?> returnType = method.getReturnType();
      
          String proxyFieldName = PROXY_METHOD + proxyMethod.id();
          CtField proxyHandler = new CtField(proxyType, proxyFieldName, proxyClass);
          proxyHandler.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
          proxyClass.addField(proxyHandler);
      
          String lambdaInvokerName = LAMBDA_SUPER + randomHexCode();
          CtMethod lambdaCall = new CtMethod(pool.get(Object.class.getName()), lambdaInvokerName, transfer(Object.class, ProxyMethod.InvokeChains.class, Object[].class), proxyClass);
          lambdaCall.setModifiers(Modifier.PUBLIC);
          boolean returnVoid = method.getReturnType() == void.class;
          lambdaCall.setBody(
              ((returnVoid ? "{" : "{Object result = ") + castPack(returnType, "super." + method.getName() + "(" + generateParam("$3", method.getParameterCount(), method.getParameterTypes()) + ")")) +
                  (returnVoid ? ";return null;}" : ";return result;}")
          );
      
          proxyClass.addMethod(lambdaCall);
          CtClass lambdaInvokerType = createLambdaInvoker(className, lambdaInvokerName, proxyMethod.id(), proxyClass);
          classes.put(lambdaInvokerType.getName(), lambdaInvokerType.toBytecode());
      
          afterSrc.append("this.").append(proxyFieldName).append(".superMethod(new ").append(lambdaInvokerType.getName()).append("(this));");
      
          CtMethod proxiedMethod = new CtMethod(pool.get(returnType.getName()), method.getName(), transfer(method.getParameterTypes()), proxyClass);
          proxiedMethod.setModifiers(method.getModifiers());
          proxiedMethod.setBody((proxiedMethod.getReturnType() == CtClass.voidType ? "{this." + proxyFieldName + ".invoke(this, $args)" :
              "{return " + cast(returnType, "this." + proxyFieldName + ".invoke(this, $args)")) + ";}");
          proxyClass.addMethod(proxiedMethod);
        }
        afterHandle.setBody("{" + afterSrc + "}");
        proxyClass.addMethod(afterHandle);
        
        CtMethod getProxyContainer = new CtMethod(containerType,"getProxyContainer", transfer(), proxyClass);
        getProxyContainer.setBody("{return proxyContainer;}");
        getProxyContainer.setModifiers(Modifier.PUBLIC);
        proxyClass.addMethod(getProxyContainer);
    
        classes.put(proxyClass.getName(), proxyClass.toBytecode());
        ByteArrayOutputStream outByte = new ByteArrayOutputStream();
        JarOutputStream out = new JarOutputStream(outByte);
        for(Map.Entry<String, byte[]> e : classes.entrySet()){
          JarEntry entry = new JarEntry(e.getKey().replace(".", "/") + ".class");
          out.putNextEntry(entry);
          out.write(e.getValue(), 0, e.getValue().length);
        }
        out.close();
        
        if(genLoader.fileExist()){
          genLoader.writeFile(genLoader.merge(outByte.toByteArray()));
        }
        else{
          genLoader.writeFile(outByte.toByteArray());
        }
        genLoader.loadJar();
        
        return loadClass(className, clazz);
      }catch(CannotCompileException | NotFoundException | IOException | ClassNotFoundException e){
        throw new RuntimeException(e);
      }
    }
  }
  
  private static CtClass[] transfer(Class<?>... classes) throws NotFoundException{
    CtClass[] result = new CtClass[classes.length];
    for(int i = 0; i < classes.length; i++){
      result[i] = pool.get(classes[i].getName());
    }
    return result;
  }
  
  private static CtClass createLambdaInvoker(String className, String lambdaInvokerName, int id, CtClass proxyClass){
    try{
      String[] str = className.split("\\.");
      StringBuilder packageName = new StringBuilder(str[0]);
      for(int i=1; i<str.length-1; i++){
        String s = str[i];
        packageName.append(".").append(s);
      }
      String lambdaName = packageName + ".$$Lambda$" + str[str.length - 1] + "$" + id;
      CtClass callSuperLambda = pool.makeClass(lambdaName);
      callSuperLambda.setModifiers(Modifier.PUBLIC);
      callSuperLambda.setInterfaces(new CtClass[]{handleType});
  
      CtField target = new CtField(proxyClass, "f$0", callSuperLambda);
      target.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
      callSuperLambda.addField(target, CtField.Initializer.byParameter(0));
  
      CtConstructor cstr = new CtConstructor(new CtClass[]{proxyClass}, callSuperLambda);
      cstr.setModifiers(Modifier.PUBLIC);
      cstr.setBody("{this.f$0 = $1;}");
      callSuperLambda.addConstructor(cstr);
  
      CtMethod invoke = new CtMethod(objectType, "invoke", transfer(Object.class, ProxyMethod.InvokeChains.class, Object[].class), callSuperLambda);
      invoke.setModifiers(Modifier.PUBLIC);
      invoke.setBody("{return this.f$0." + lambdaInvokerName + "($$);}");
      callSuperLambda.addMethod(invoke);
      return callSuperLambda;
    }catch(NotFoundException | CannotCompileException e){
      throw new RuntimeException(e);
    }
  }
  
  private String generateParam(String targetArr, int length, Class<?>... types){
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for(int i = 0; i < length; i++){
      result.append(first ? "" : ", ").append(cast(types[i], targetArr + "[" + i + "]"));
      first = false;
    }
    return result.toString();
  }
  
  private String cast(Class<?> clazz, String source){
    return "((" + asPacket(clazz).getName() + ") " + source + ")" + (clazz == boolean.class? ".booleanValue()":
        clazz == int.class? ".intValue()":
        clazz == float.class? ".floatValue()":
        clazz == byte.class? ".byteValue()":
        clazz == long.class? ".longValue()":
        clazz == double.class? ".doubleValue()":
        clazz == short.class? "shortValue()":
        clazz == char.class? "charValue()":
            "");
  }
  
  private String castPack(Class<?> clazz, String source){
    return clazz == boolean.class? "Boolean.valueOf((" + clazz.getName() + ") " + source + ")":
        clazz == int.class? "Boolean.valueOf((" + clazz.getName() + ") " + source + ")":
        clazz == float.class? "Boolean.valueOf((" + clazz.getName() + ") " + source + ")":
        clazz == byte.class? "Boolean.valueOf((" + clazz.getName() + ") " + source + ")":
        clazz == long.class? "Boolean.valueOf((" + clazz.getName() + ") " + source + ")":
        clazz == double.class? "Boolean.valueOf((" + clazz.getName() + ") " + source + ")":
        clazz == short.class? "Boolean.valueOf((" + clazz.getName() + ") " + source + ")":
        clazz == char.class? "Boolean.valueOf((" + clazz.getName() + ") " + source + ")":
            source;
  }
  
  private Class<?> asPacket(Class<?> clazz){
    return clazz == boolean.class? Boolean.class:
        clazz == int.class? Integer.class:
        clazz == float.class? Float.class:
        clazz == byte.class? Byte.class:
        clazz == long.class? Long.class:
        clazz == double.class? Double.class:
        clazz == short.class? Short.class:
        clazz == char.class? Character.class:
            clazz;
  }
}
