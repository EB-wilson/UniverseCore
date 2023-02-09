package universecore.desktopcore.classes;

import arc.Core;
import arc.files.Fi;
import arc.files.ZipFi;
import arc.struct.ObjectSet;
import arc.util.Log;
import universecore.util.classes.BaseGeneratedClassLoader;
import universecore.util.handler.MethodHandler;
import universecore.util.mods.ModInfo;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class DesktopGeneratedClassLoader extends BaseGeneratedClassLoader{
  private static final Fi jarFileCache = Core.settings.getDataDirectory().child("universecore").child("cache");
  public static final Fi TMP_FILE = jarFileCache.child("temp_file.jar");

  private ZipFi zip;
  private Class<?> currAccessor;

  public DesktopGeneratedClassLoader(ModInfo mod, ClassLoader parent){
    super(mod, parent);
  }

  @Override
  public void setAccessor(Class<?> accessor){
    currAccessor = accessor;
  }

  @Override
  public void declareClass(String name, byte[] byteCode){
    Fi select = null;

    if(zip != null){
      String[] paths = name.split("\\.");
      for(int i = 0; i < paths.length; i++){
        select = zip.child(paths[i] + (i == paths.length - 1 ? ".class" : ""));
      }

      assert select != null;
      if(select.exists()) return;
    }

    boolean existed = file.exists();
    if(existed){
      new Fi(file).copyTo(TMP_FILE);
    }
    try(ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(file))){
      String entryName = name.replace(".", "/") + ".class";

      ObjectSet<String> added = new ObjectSet<>();

      ZipEntry declaringEntry = new ZipEntry(entryName), entry = declaringEntry;
      added.add(entry.getName());

      if(existed){
        try {
          ZipFile tempZipped = new ZipFile(TMP_FILE.file());

          Enumeration<? extends ZipEntry> entries = tempZipped.entries();
          while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            if (entry.isDirectory() || !added.add(entry.getName())) continue;

            outputStream.putNextEntry(new ZipEntry(entry));
            try (InputStream inputStream = tempZipped.getInputStream(entry)) {
              for (int l = inputStream.read(); l > -1; l = inputStream.read()) {
                outputStream.write(l);
              }
              outputStream.closeEntry();
              outputStream.flush();
            }
          }

          tempZipped.close();
        }
        catch (ZipException e){
          Log.warn("[GeneratedClassLoader] cache zip format error or it was an empty zip, direct write byte code");
        }finally {
          TMP_FILE.delete();
        }
      }

      outputStream.putNextEntry(declaringEntry);
      outputStream.write(byteCode);
      outputStream.closeEntry();
      outputStream.finish();
      outputStream.flush();

      zip = new ZipFi(new Fi(file));
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException{
    try{
      return super.findClass(name);
    }catch(ClassNotFoundException ignored){
      String[] classPath = name.split("\\.");
      classPath[classPath.length - 1] = classPath[classPath.length - 1] + ".class";

      Fi f = zip;
      for(String path: classPath){
        f = f.child(path);
      }

      try(InputStream in = f.read()){
        ByteArrayOutputStream w = new ByteArrayOutputStream();
        int i;
        while((i = in.read()) != -1){
          w.write(i);
        }
        byte[] byteCode = w.toByteArray();

        Class<?> res;
        if(currAccessor != null){
          ClassLoader loader = currAccessor.getClassLoader();
          ProtectionDomain domain = currAccessor.getProtectionDomain();

          res = MethodHandler.invokeDefault(ClassLoader.class, "defineClass0",
              loader,
              currAccessor,
              name,
              byteCode, 0, byteCode.length,
              domain,
              false,
              Modifier.PUBLIC,
              null
          );
          currAccessor = null;
        }
        else {
          res = defineClass(name, byteCode, 0, byteCode.length);
        }

        return res;
      }catch(IOException ignored1){
        throw new ClassNotFoundException("no such class: " + name);
      }
    }
  }

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
    Class<?> c = super.loadClass(name);
    if(c == null){
      c = findClass(name);
      if(resolve) resolveClass(c);
    }

    return c;
  }
}
