import dynamilize.DynamicClass;
import dynamilize.DynamicMaker;
import dynamilize.DynamicObject;
import dynamilize.classmaker.ASMGenerator;
import dynamilize.classmaker.BaseClassLoader;
import dynamilize.classmaker.ClassInfo;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DynamicTest{
  public static void main(String[] args){
    DynamicClass dyc = DynamicClass.get("Demo");                              //首先，传入类型进行行为描述

    BaseClassLoader loader = new BaseClassLoader(DynamicMaker.class.getClassLoader());
    ASMGenerator generator = new ASMGenerator(loader, Opcodes.V1_8);
    DynamicMaker maker = new DynamicMaker(acc -> acc.setAccessible(true)){
      @Override
      protected <T> Class<? extends T> generateClass(Class<T> baseClass, Class<?>[] interfaces){
        ClassInfo<?> i = makeClassInfo(baseClass, interfaces);
        
        File f = new File("temp", baseClass.getSimpleName() + ".class");
        f.getParentFile().mkdirs();

        try{
          f.createNewFile();
          new FileOutputStream(f).write(generator.genByteCode(i));
        }catch(IOException e){
          throw new RuntimeException(e);
        }

        return (Class<? extends T>) i.generate(generator);
      }
    };

    dyc.setFunction("run", (s, superPointer, a) -> {
      superPointer.invokeFunc("run", a);
    }, long.class);

    DynamicObject<Runner> r = maker.newInstance(Runner.class, dyc, "abc", 78);

    maker.newInstance(r.getClass(), dyc, "erf", 88).invokeFunc("run");
  }

  public static class Runner{
    public String name;
    public int time;

    public Runner(String name, int time){
      this.name = name;
      this.time = time;
      run();
    }

    public void run(){
      System.out.println(name + ":" + time);
    }
  }
}

