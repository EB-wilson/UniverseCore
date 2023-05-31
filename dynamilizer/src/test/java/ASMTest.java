import dynamilize.classmaker.*;
import dynamilize.classmaker.code.ILocal;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class ASMTest{
  public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException{
    ClassInfo<?> typ = new ClassInfo<>(
        Modifier.PUBLIC,
        "main.Test",
        null,
        ClassInfo.asType(Runnable.class)
    );

    ClassInfo<System> sys = ClassInfo.asType(System.class);
    ClassInfo<PrintStream> print = ClassInfo.asType(PrintStream.class);
    ClassInfo<Test> testType = ClassInfo.asType(Test.class);

    FieldInfo<PrintStream> prF = sys.getField(print, "out");
    MethodInfo<PrintStream, Void> printf = print.getMethod(ClassInfo.VOID_TYPE, "println", ClassInfo.OBJECT_TYPE);

    FieldInfo<Test> inte = typ.declareField(
        Modifier.PRIVATE | Modifier.STATIC,
        "field",
        testType,
        Test.a
    );

    CodeBlock<Void> met = typ.declareMethod(
        Modifier.PUBLIC,
        "run",
        ClassInfo.VOID_TYPE
    );
    ILocal<PrintStream> out = met.local(print);
    met.assign(null, prF, out);

    ILocal<Test> str = met.local(testType);
    met.assign(null, inte, str);
    met.invoke(out, printf, null, str);

    byte[] b = new ASMGenerator(new BaseClassLoader(null), Opcodes.V1_8).genByteCode(typ);
    File cla = new File("temp/Output.class");
    try{
      cla.getParentFile().mkdirs();
      FileOutputStream o = new FileOutputStream(cla, false);
      o.write(b);
      o.flush();
    }catch(IOException e){
      throw new RuntimeException(e);
    }

    Class<?> c = typ.generate(new ASMGenerator(new BaseClassLoader(Demo.class.getClassLoader()), Opcodes.V1_8));
    Constructor<?> cstr = c.getDeclaredConstructor();

    ((Runnable)cstr.newInstance()).run();
  }

  public enum Test{
    a, b, c
  }
}

