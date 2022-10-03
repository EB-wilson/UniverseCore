package universecore;

import arc.util.Log;
import com.android.dex.DexFormat;
import mindustry.mod.Mod;
import universecore.androidcore.AndroidFieldAccessHelper;
import universecore.androidcore.AndroidMethodInvokeHelper;
import universecore.androidcore.handler.AndroidClassHandler;
import universecore.util.FieldAccessHelper;
import universecore.util.MethodInvokeHelper;
import universecore.util.mods.IllegalModHandleException;
import universecore.util.mods.ModGetter;
import universecore.util.mods.ModInfo;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class SetPlatformImpl{
  @SuppressWarnings({"unchecked", "ConstantConditions"})
  public static void setImplements(){
    try{
      Class.forName("java.lang.invoke.MethodHandle");

      try{
        Log.info(SetPlatformImpl.class.getResource("").getFile());
        String[] path = SetPlatformImpl.class.getResource("").getFile().split("/");
        StringBuilder builder = new StringBuilder(path[0].replace("file:", ""));
        for(int i = 1; i < path.length; i++){
          builder.append("/").append(path[i]);
          if(path[i].contains(".jar!") || path[i].contains(".zip!")) break;
        }
        File selfFile = new File(builder.substring(0, builder.length() - 1));
        JarFile jf = new JarFile(selfFile);
        InputStream in = jf.getInputStream(new JarEntry("android-api-26.jar"));

        ClassLoader loader;
        try{
          Class<? extends ClassLoader> inMemLoaderType =
              (Class<? extends ClassLoader>) Class.forName("dalvik.system.InMemoryDexClassLoader");

          Constructor<ClassLoader> cstr =
              (Constructor<ClassLoader>) inMemLoaderType.getConstructor(ByteBuffer.class, ClassLoader.class);

          JarInputStream classesReader = new JarInputStream(in);
          while(true) if(classesReader.getNextJarEntry().getName().equals(DexFormat.DEX_IN_JAR_NAME)) break;

          ArrayList<Byte> byteArr = new ArrayList<>();
          byteArr.clear();
          for(int i = classesReader.read(); i != -1; i = classesReader.read()){
            byteArr.add((byte) i);
          }

          byte[] bytes = new byte[byteArr.size()];
          for(int i = 0; i < byteArr.size(); i++){
            bytes[i] = byteArr.get(i);
          }

          loader = cstr.newInstance(ByteBuffer.wrap(bytes), SetPlatformImpl.class.getClassLoader());
        }catch(ClassNotFoundException ignored){
          Class<? extends ClassLoader> loaderType =
              (Class<? extends ClassLoader>) Class.forName("dalvik.system.DexClassLoader");

          Constructor<ClassLoader> cstr =
              (Constructor<ClassLoader>) loaderType.getConstructor(String.class, String.class, String.class, ClassLoader.class);

          File tmpFile = new File(selfFile.getParent(), "tmp/android-api-26.jar");
          tmpFile.getParentFile().mkdirs();
          tmpFile.createNewFile();

          ArrayList<Byte> byteArr = new ArrayList<>();
          for(int i = in.read(); i != -1; i = in.read()){
            byteArr.add((byte) i);
          }
          byte[] bytes = new byte[byteArr.size()];
          for(int i = 0; i < byteArr.size(); i++){
            bytes[i] = byteArr.get(i);
          }

          FileOutputStream copyTo = new FileOutputStream(tmpFile);
          copyTo.write(bytes);
          copyTo.flush();
          copyTo.close();

          loader = cstr.newInstance(tmpFile.getPath(), tmpFile.getPath() + "/oct", null, SetPlatformImpl.class.getClassLoader());

          tmpFile.getParentFile().deleteOnExit();
        }

        Class<? extends FieldAccessHelper> fieldAccess26Type =
            (Class<? extends FieldAccessHelper>) loader.loadClass("universecore.android26core.AndroidFieldAccessHelper26");
        Class<? extends MethodInvokeHelper> methodInvoke26Type =
            (Class<? extends MethodInvokeHelper>) loader.loadClass("universecore.android26core.AndroidMethodInvokeHelper26");

        Constructor<? extends FieldAccessHelper> faCstr = fieldAccess26Type.getConstructor();
        Constructor<? extends MethodInvokeHelper> miCstr = methodInvoke26Type.getConstructor();

        ImpCore.fieldAccessHelper = faCstr.newInstance();
        ImpCore.methodInvokeHelper = miCstr.newInstance();
      }catch(ClassNotFoundException|NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException|IOException e){
        throw new RuntimeException(e);
      }
    }catch(ClassNotFoundException ignored){
      ImpCore.fieldAccessHelper = new AndroidFieldAccessHelper();
      ImpCore.methodInvokeHelper = new AndroidMethodInvokeHelper();
    }

    ImpCore.classes = modMain -> {
      try{
        if(!Mod.class.isAssignableFrom(modMain))
          throw new IllegalModHandleException("class was not a mod main class");

        ModInfo mod = ModGetter.getModWithClass((Class<? extends Mod>) modMain);
        if(mod == null)
          throw new IllegalModHandleException("mod with main class " + modMain + " was not found");

        ModGetter.checkModFormat(mod.file);
        return new AndroidClassHandler(mod);
      }catch(IllegalModHandleException e){
        throw new RuntimeException(e);
      }
    };
  }
}
