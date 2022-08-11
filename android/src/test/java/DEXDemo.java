import dynamilize.DynamicClass;
import dynamilize.DynamicMaker;
import dynamilize.classmaker.ByteClassLoader;
import universecore.androidcore.classes.DexGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class DEXDemo{
  public static void main(String[] args){
    DynamicMaker maker = new DynamicMaker(e -> e.setAccessible(true)){
      @Override
      protected <T> Class<? extends T> generateClass(Class<T> baseClass, Class<?>[] interfaces){
        return makeClassInfo(baseClass, interfaces).generate(new DexGenerator(new ByteClassLoader(){
          @Override
          public void declareClass(String name, byte[] byteCode){
            File file = new File("temp", name + ".dex");
            file.mkdirs();
            file.delete();
            try{
              FileOutputStream os;
              file.createNewFile();
              (os = new FileOutputStream(file)).write(byteCode);
              os.flush();
            }catch(IOException e){
              throw new RuntimeException(e);
            }
          }

          @Override
          public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
            throw new ClassNotFoundException();
          }
        }));
      }
    };

    maker.newInstance(HashMap.class, DynamicClass.get("Demo"));
  }
}
