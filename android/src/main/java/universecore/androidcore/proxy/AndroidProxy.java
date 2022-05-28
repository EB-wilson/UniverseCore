package universecore.androidcore.proxy;

import com.android.dx.*;
import com.android.dx.rop.code.RegisterSpec;
import com.android.dx.rop.code.RegisterSpecList;
import com.android.dx.rop.code.Rops;
import com.android.dx.rop.code.ThrowingInsn;
import universecore.androidcore.handler.AndroidClassHandler;
import universecore.util.classes.AbstractFileClassLoader;
import universecore.util.handler.FieldHandler;
import universecore.util.handler.MethodHandler;
import universecore.util.proxy.BaseProxy;
import universecore.util.proxy.IProxied;
import universecore.util.proxy.InvokeChains;
import universecore.util.proxy.ProxyHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static java.lang.reflect.Modifier.PUBLIC;

/**Proxy的安卓实现，除非你确定自己的mod只用于安卓，否则不要直接引用这个类型
 * @author EBwilson
 * @since 1.2*/
@SuppressWarnings({"unchecked", "rawtypes"})
public class AndroidProxy<Target> extends BaseProxy<Target>{
  private static int proxyCounter;
  
  private static final TypeId<BaseProxy> containerType = TypeId.get(BaseProxy.class);
  private static final TypeId<ProxyMethod> proxyType = TypeId.get(ProxyMethod.class);
  private static final TypeId<InvokeChains> invokeType = TypeId.get(InvokeChains.class);
  private static final TypeId<ProxyHandler> handleType = TypeId.get(ProxyHandler.class);
  
  private static final TypeId<Boolean> booleanType = TypeId.get(Boolean.class);
  private static final TypeId<Integer> integerType = TypeId.get(Integer.class);
  private static final TypeId<Float> floatType = TypeId.get(Float.class);
  private static final TypeId<Byte> byteType = TypeId.get(Byte.class);
  private static final TypeId<Short> shortType = TypeId.get(Short.class);
  private static final TypeId<Long> longType = TypeId.get(Long.class);
  private static final TypeId<Double> doubleType = TypeId.get(Double.class);
  private static final TypeId<Character> characterType = TypeId.get(Character.class);
  
  private static final MethodId<Boolean, Boolean> booleanValue = booleanType.getMethod(TypeId.BOOLEAN, "booleanValue");
  private static final MethodId<Integer, Integer> intValue = integerType.getMethod(TypeId.INT, "intValue");
  private static final MethodId<Float, Float> floatValue = floatType.getMethod(TypeId.FLOAT, "floatValue");
  private static final MethodId<Byte, Byte> byteValue = byteType.getMethod(TypeId.BYTE, "byteValue");
  private static final MethodId<Short, Short> shortValue = shortType.getMethod(TypeId.SHORT, "shortValue");
  private static final MethodId<Long, Long> longValue = longType.getMethod(TypeId.LONG, "longValue");
  private static final MethodId<Double, Double> doubleValue = doubleType.getMethod(TypeId.DOUBLE, "doubleValue");
  private static final MethodId<Character, Character> charValue = characterType.getMethod(TypeId.CHAR, "charValue");
  
  private static final MethodId<Boolean, Boolean> valueOfB = booleanType.getMethod(booleanType, "valueOf", TypeId.BOOLEAN);
  private static final MethodId<Integer, Integer> valueOfI = integerType.getMethod(integerType, "valueOf", TypeId.INT);
  private static final MethodId<Float, Float> valueOfF = floatType.getMethod(floatType, "valueOf", TypeId.FLOAT);
  private static final MethodId<Byte, Byte> valueOfBY = byteType.getMethod(byteType, "valueOf", TypeId.BYTE);
  private static final MethodId<Short, Short> valueOfS = shortType.getMethod(shortType, "valueOf", TypeId.SHORT);
  private static final MethodId<Long, Long> valueOfL = longType.getMethod(longType, "valueOf", TypeId.LONG);
  private static final MethodId<Double, Double> valueOfD = doubleType.getMethod(doubleType, "valueOf", TypeId.DOUBLE);
  private static final MethodId<Character, Character> valueOfC = characterType.getMethod(characterType, "valueOf", TypeId.CHAR);
  
  private final DexMaker proxyMaker = new DexMaker();
  private final File file;
  
  public AndroidProxy(Class<Target> clazz, AbstractFileClassLoader loader, AndroidClassHandler handler){
    super(clazz, loader, handler);
    file = loader.getFile();
  }
  
  @Override
  protected <T extends Target> Class<T> generateProxyClass(){
    //public class className$Proxy$# extends className
    String className = clazz.getName() + "$Proxy$" + proxyCounter++;
    try{
      return loadClass(className, clazz);
    }catch(ClassNotFoundException ignored){
      TypeId<Target> superType = asType(clazz);
      TypeId<T> type = TypeId.get("L" + className.replace(".", "/") + ";");
      proxyMaker.declare(type, className + ".gen", Modifier.PUBLIC, superType, transfer(IProxied.class));
      
      FieldId<T, BaseProxy> proxyContainer = type.getField(containerType, "proxyContainer");
      proxyMaker.declare(proxyContainer, Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL, null);
      
      makeConstructor(type, superType);
      for(java.lang.reflect.Constructor<? extends Target> value : assignedCstr){
        makeConstructor(type, superType, value.getParameterTypes());
      }
  
      MethodId<T, Void> afterInvoker = type.getMethod(TypeId.VOID, "afterHandle");
      Code code = proxyMaker.declare(afterInvoker, Modifier.PUBLIC);
      Local<ProxyMethod> proxyLocal = code.newLocal(proxyType);
      Local<ProxyHandler> handler = code.newLocal(handleType);
      if(superProxy != null){
        MethodId superHandle = TypeId.get(superProxy.proxyClass).getMethod(TypeId.VOID, "afterHandle");
        code.invokeSuper(superHandle, null, code.getThis(type));
      }
      
      MethodId<T, BaseProxy> getProxyContainer = type.getMethod(containerType, "getProxyContainer");
      Code getC = proxyMaker.declare(getProxyContainer, Modifier.PUBLIC);
      Local<BaseProxy> res = getC.newLocal(containerType);
      getC.sget(proxyContainer, res);
      getC.returnValue(res);
      
      MethodId<ProxyMethod, Object> invokeMethod = proxyType.getMethod(TypeId.OBJECT, "invoke", TypeId.OBJECT, TypeId.get(Object[].class));
      for(ProxyMethod<?, Target> proxyMethod : proxies.values()){
        FieldId<T, ProxyMethod> proxyMethodF = type.getField(proxyType, PROXY_METHOD + proxyMethod.id());
        proxyMaker.declare(proxyMethodF, Modifier.PRIVATE | Modifier.FINAL, null);
    
        Method m = proxyMethod.targetMethod();
        TypeId<?> returnType = asType(m.getReturnType());
        TypeId<?>[] ats = transfer(m.getParameterTypes());
        MethodId<T, ?> proxiedMethod = type.getMethod(returnType, m.getName(), ats);
        MethodId<Target, ?> proxiedMethodSuper = asType(clazz).getMethod(returnType, m.getName(), ats);
        MethodId<T, ?> superInvoker = type.getMethod(TypeId.OBJECT, LAMBDA_SUPER + randomHexCode(), transfer(Object.class, InvokeChains.class, Object[].class));
    
        //模拟生成lambda方法引用
        //public Object $lambdaSuper$#(Object self, InvokeChains callSuper, Object... args){
        //  return super.*proxiedMethod*(args[0], args[1],...);
        //}
        Code callSuperBody = proxyMaker.declare(superInvoker, Modifier.PUBLIC);
        Local<Object> nullR = callSuperBody.newLocal(TypeId.OBJECT);
        Local tmp = callSuperBody.newLocal(TypeId.get(m.getReturnType()));
        Local<Object> result = callSuperBody.newLocal(TypeId.OBJECT);
        Local<Object[]> argArr = callSuperBody.getParameter(2, TypeId.get(Object[].class));
        Local<?>[] parameter = expandArrParam(callSuperBody, argArr, ats);
    
        if(returnType == TypeId.VOID){
          callSuperBody.invokeSuper(proxiedMethodSuper, null, callSuperBody.getThis(type), parameter);
          callSuperBody.loadConstant(nullR, null);
          callSuperBody.move(result, nullR);
          callSuperBody.returnValue(result);
        }else{
          callSuperBody.invokeSuper(proxiedMethodSuper, tmp, callSuperBody.getThis(type), parameter);
          returnValue(callSuperBody, result, TypeId.OBJECT, tmp, TypeId.get(m.getReturnType()));
        }
    
        //@Override
        //public *returnType* methodName(args){
        //  return $proxy$.invoke(this, args);
        //}
        Code proxiedBody = proxyMaker.declare(proxiedMethod, Modifier.PUBLIC);
        Local<T> self = proxiedBody.getThis(type);
        Local<ProxyMethod> pm = proxiedBody.newLocal(proxyType);
        Local temp = returnType == TypeId.VOID ? null : proxiedBody.newLocal(toPacked(returnType));
        Local ret = returnType == TypeId.VOID ? null : proxiedBody.newLocal(TypeId.OBJECT);
        Local returnValue = returnType == TypeId.VOID ? null : proxiedBody.newLocal(returnType);
        Local<Object[]> args = getArgsArr(proxiedBody, ats);
        proxiedBody.iget(proxyMethodF, pm, self);
        proxiedBody.invokeVirtual(invokeMethod, ret, pm, self, args);
        if(returnType == TypeId.VOID){
          proxiedBody.returnVoid();
        }else{
          proxiedBody.cast(temp, ret);
          returnValue(proxiedBody, returnValue, returnType, temp, temp.getType());
        }
    
        //@Override
        //public void afterHand(){
        //  ......
        //  this.$proxy$.superMethod((self, superMethod, args) -> super(args));
        //}
        TypeId<ProxyHandler> lambdaType = declareLambda(className, proxyMethod.id(), type, superInvoker);
        MethodId<ProxyHandler, Void> lambdaCstr = lambdaType.getConstructor(type);
        MethodId<ProxyMethod, Void> setSuperMethod = proxyType.getMethod(TypeId.VOID, "superMethod", transfer(ProxyHandler.class));
        self = code.getThis(type);
        code.iget(proxyMethodF, proxyLocal, self);
        code.newInstance(handler, lambdaCstr, self);
        code.invokeVirtual(setSuperMethod, null, proxyLocal, handler);
      }
      code.returnVoid();
      try{
        byte[] dex = proxyMaker.generate();
        
        if(genLoader.fileExist()){
          genLoader.writeFile(genLoader.merge(dex));
        }
        else{
          file.createNewFile();
          genLoader.writeFile(dex);
        }
        genLoader.loadJar();
        
        return loadClass(className, clazz);
      }catch(ClassNotFoundException | IOException e){
        throw new RuntimeException(e);
      }
    }
  }
  
  /**创建lambda的回调函数，lambda将回调调用此处创建的方法*/
  private <D extends ProxyHandler, V> TypeId<D> declareLambda(String className, int id, TypeId<V> argType, MethodId<V, ?> lambdaInvoke){
    String[] str = className.split("\\.");
    StringBuilder packageName = new StringBuilder();
    for(int i=0; i<str.length-1; i++){
      packageName.append(i==0? "": ".").append(str[i]);
    }
    String lambdaName = packageName + ".$$Lambda$" + str[str.length - 1] + "$" + id;
    
    TypeId<D> superInvokerType = TypeId.get("L" + lambdaName.replace(".", "/")  + ";");
    proxyMaker.declare(superInvokerType, lambdaName + ".gen", Modifier.PUBLIC, TypeId.OBJECT, handleType);
    FieldId<D, V> obj = superInvokerType.getField(argType, "f$0");
    proxyMaker.declare(obj, Modifier.PUBLIC | Modifier.FINAL, null);
    
    MethodId<D, Void> cstr = superInvokerType.getConstructor(argType);
    Code cstrBody = proxyMaker.declare(cstr, Modifier.PUBLIC);
    Local<V> arg = cstrBody.getParameter(0, argType);
    MethodId<Object, ?> superConstructor = TypeId.OBJECT.getConstructor();
    cstrBody.invokeDirect(superConstructor, null, cstrBody.getThis(superInvokerType));
    cstrBody.iput(obj, cstrBody.getThis(superInvokerType), arg);
    cstrBody.returnVoid();
    
    MethodId<D, Object> invoke = superInvokerType.getMethod(TypeId.OBJECT, "invoke", TypeId.OBJECT, invokeType, TypeId.get(Object[].class));
    Code invokeBody = proxyMaker.declare(invoke, Modifier.PUBLIC);
    Local<Object> returnValue = invokeBody.newLocal(TypeId.OBJECT);
    Local<V> inst = invokeBody.newLocal(argType);
    Local<?>[] param = getParamLocals(invokeBody, transfer(Object.class, InvokeChains.class, Object[].class));
    
    invokeBody.iget(obj, inst, invokeBody.getThis(superInvokerType));
    invokeBody.invokeVirtual(lambdaInvoke, returnValue, inst, param);
    invokeBody.returnValue(returnValue);
    
    return superInvokerType;
  }
  
  private <T extends Target> void makeConstructor(TypeId<T> type, TypeId<Target> superType, Class<?>...paramTypes){
    TypeId<?>[] argTypes = transfer(paramTypes);
    MethodId<T,?> constructor = type.getConstructor(argTypes);
    Code constructorCode = proxyMaker.declare(constructor, PUBLIC);
    Local<?>[] params = getParamLocals(constructorCode, argTypes);
    MethodId<Target, ?> superConstructor = superType.getConstructor(argTypes);
    constructorCode.invokeDirect(superConstructor, null, constructorCode.getThis(type), params);
    constructorCode.returnVoid();
  }
  
  private static Local<Object[]> getArgsArr(Code code, TypeId<?>... paramType){
    Local<?>[] args = new Local[paramType.length];
    Local<Object[]> result = code.newLocal(TypeId.get(Object[].class));
    Local<Integer> temp = code.newLocal(TypeId.INT);
    for(int i = 0; i < paramType.length; i++){
      args[i] = code.newLocal(toPacked(paramType[i]));
    }
    
    code.loadConstant(temp, paramType.length);
    code.newArray(result, temp);
    Local param;
    for(int i = 0; i < paramType.length; i++){
      param = code.getParameter(i, paramType[i]);
      castPack(code, args[i], args[i].getType(), param, param.getType(), false);
      code.loadConstant(temp, i);
      //code.aput(result, temp, args[i]); TODO:臭猫的游戏本身携带的dx版本过旧没有此方法，用替代方案进行
      aput(code, result, temp, args[i]);
    }
    return result;
  }

  private static void aput(Code code, Local<?> array, Local<Integer> index, Local<?> source) {
    MethodHandler.invokeDefault(code, "addInstruction", Void.class,
        new ThrowingInsn(
            Rops.opAput(
                FieldHandler.getValueDefault(source.getType(), "ropType")
            ),
            FieldHandler.getValueDefault(code, "sourcePosition"), make(
                MethodHandler.invokeDefault(source, "spec", RegisterSpec.class),
                MethodHandler.invokeDefault(array, "spec", RegisterSpec.class),
                MethodHandler.invokeDefault(index, "spec", RegisterSpec.class)
            ), FieldHandler.getValueDefault(code, "catches")
        )
    );
  }

  private static RegisterSpecList make(RegisterSpec spec0, RegisterSpec spec1, RegisterSpec spec2) {
    RegisterSpecList result = new RegisterSpecList(3);
    result.set(0, spec0);
    result.set(1, spec1);
    result.set(2, spec2);
    return result;
  }

  private static Local<?>[] getParamLocals(Code code, TypeId<?>... types){
    Local<?>[] r = new Local[types.length];
    Local<?>[] result = new Local[types.length];
    for(int i = 0; i < result.length; i++){
      r[i] = code.newLocal(toPacked(types[i]));
      result[i] = code.newLocal(types[i]);
    }
    Local param;
    for(int i = 0; i < result.length; i++){
      param = code.getParameter(i, types[i]);
      code.cast(r[i], param);
      cast(code, result[i], types[i], r[i], r[i].getType(), false);
    }
    return result;
  }
  
  private static Local<?>[] expandArrParam(Code code, Local<Object[]> array, TypeId<?>... paramType){
    Local<?>[] r = new Local[paramType.length];
    Local<?>[] result = new Local[paramType.length];
    Local<Object> temp = code.newLocal(TypeId.OBJECT);
    Local<Integer> index = code.newLocal(TypeId.INT);
    for(int i = 0; i < result.length; i++){
      r[i] = code.newLocal(toPacked(paramType[i]));
      result[i] = code.newLocal(paramType[i]);
    }
    for(int i = 0; i < result.length; i++){
      code.loadConstant(index, i);
      code.aget(temp, array, index);
      code.cast(r[i], temp);
      cast(code, result[i], paramType[i], r[i], r[i].getType(), false);
    }
    return result;
  }
  
  private static void returnValue(Code code, Local result, TypeId returnType, Local source, TypeId sourceType){
    if(returnType == TypeId.VOID) code.returnVoid();
    else if(returnType == TypeId.OBJECT || isPacked(returnType)){
      castPack(code, result, returnType, source, sourceType, false);
      code.returnValue(result);
    }
    else{
      cast(code, result, returnType, source, sourceType, true);
      code.returnValue(result);
    }
  }
  
  private static void castPack(Code code, Local target, TypeId type, Local source, TypeId sourceType, boolean force){
    if(type != TypeId.OBJECT && !isPacked(type)){
      code.cast(target, source);
    }
    else if(sourceType == TypeId.BOOLEAN) code.invokeStatic(valueOfB, target, source);
    else if(sourceType == TypeId.INT) code.invokeStatic(valueOfI, target, source);
    else if(sourceType == TypeId.FLOAT) code.invokeStatic(valueOfF, target, source);
    else if(sourceType == TypeId.BYTE) code.invokeStatic(valueOfBY, target, source);
    else if(sourceType == TypeId.LONG) code.invokeStatic(valueOfL, target, source);
    else if(sourceType == TypeId.DOUBLE) code.invokeStatic(valueOfD, target, source);
    else if(sourceType == TypeId.SHORT) code.invokeStatic(valueOfS, target, source);
    else if(sourceType == TypeId.CHAR) code.invokeStatic(valueOfC, target, source);
    else if(force) code.cast(target, source);
    else code.move(target, source);
  }
  
  private static void cast(Code code, Local target, TypeId type, Local source, TypeId sourceType, boolean force){
    if(type == TypeId.BOOLEAN && sourceType == booleanType) code.invokeVirtual(booleanValue, target, source);
    else if(type == TypeId.INT && sourceType == integerType) code.invokeVirtual(intValue, target, source);
    else if(type == TypeId.FLOAT && sourceType == floatType) code.invokeVirtual(floatValue, target, source);
    else if(type == TypeId.BYTE && sourceType == byteType) code.invokeVirtual(byteValue, target, source);
    else if(type == TypeId.LONG && sourceType == longType) code.invokeVirtual(longValue, target, source);
    else if(type == TypeId.DOUBLE && sourceType == doubleType) code.invokeVirtual(doubleValue, target, source);
    else if(type == TypeId.SHORT && sourceType == shortType) code.invokeVirtual(shortValue, target, source);
    else if(type == TypeId.CHAR && sourceType == characterType) code.invokeVirtual(charValue, target, source);
    else if(force) code.cast(target, source);
    else code.move(target, source);
  }
  
  private static TypeId<?>[] transfer(Class<?>... classes){
    TypeId<?>[] result = new TypeId[classes.length];
    for(int i = 0; i < result.length; i++){
      result[i] = asType(classes[i]);
    }
    return result;
  }
  
  private static boolean isPacked(TypeId type){
    return type == booleanType || type == integerType ||
    type == floatType || type == longType ||
    type == doubleType || type == shortType ||
    type == byteType || type == characterType;
  }
  
  private static TypeId toPacked(TypeId type){
    return type == TypeId.BOOLEAN? booleanType:
        type == TypeId.INT? integerType:
        type == TypeId.FLOAT? floatType:
        type == TypeId.LONG? longType:
        type == TypeId.DOUBLE? doubleType:
        type == TypeId.SHORT? shortType:
        type == TypeId.BYTE? byteType:
        type == TypeId.CHAR? characterType:
            type;
  }
  
  private static TypeId asType(Class<?> clazz){
    return clazz == void.class || clazz == Void.class? TypeId.VOID:
        clazz == boolean.class || clazz == Boolean.class? TypeId.BOOLEAN:
        clazz == int.class || clazz == Integer.class? TypeId.INT:
        clazz == float.class || clazz == Float.class? TypeId.FLOAT:
        clazz == long.class || clazz == Long.class? TypeId.LONG:
        clazz == double.class || clazz == Double.class? TypeId.DOUBLE:
        clazz == short.class || clazz == Short.class? TypeId.SHORT:
        clazz == byte.class || clazz == Byte.class? TypeId.BYTE:
        clazz == char.class || clazz == Character.class? TypeId.CHAR:
            TypeId.get(clazz);
  }
}
