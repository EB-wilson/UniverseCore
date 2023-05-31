package universecore.desktopcore.classes;

import arc.Core;
import arc.files.Fi;
import arc.files.ZipFi;
import arc.util.ArcRuntimeException;
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
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class DesktopGeneratedClassLoader extends BaseGeneratedClassLoader{
  private static final Fi jarFileCache = Core.settings.getDataDirectory().child("universecore").child("cache");
  public static final Fi TMP_FILE = jarFileCache.child("temp_file.jar");

  private final HashMap<String, Class<?>> classMap = new HashMap<>();

  private ZipFi zip;
  private Class<?> currAccessor;

  public DesktopGeneratedClassLoader(ModInfo mod, ClassLoader parent){
    super(mod, parent);
  }

  @Override
  public void declareClass(String name, byte[] byteCode){
    if(classMap.containsKey(name)) return;

    boolean existed;
    try {
      existed = !file.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if(existed){
      zip = new ZipFi(new Fi(file));
      new Fi(file).copyTo(TMP_FILE);
    }

    try(ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(file, false))){
      String entryName = name.replace(".", "/") + ".class";

      ZipEntry declaringEntry = new ZipEntry(entryName);

      if(existed){
        try {
          ZipFile tempZipped = new ZipFile(TMP_FILE.file());

          Enumeration<? extends ZipEntry> entries = tempZipped.entries();
          while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) continue;

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
    Class<?> res = classMap.computeIfAbsent(name, n -> {
      String[] classPath = n.split("\\.");
      classPath[classPath.length - 1] = classPath[classPath.length - 1] + ".class";

      if (zip == null){
        if (!file.exists())
          return null;

        zip = new ZipFi(new Fi(file));
      }

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

        Class<?> c;
        if(currAccessor != null){
          ClassLoader loader = currAccessor.getClassLoader();
          ProtectionDomain domain = currAccessor.getProtectionDomain();

          c = MethodHandler.invokeDefault(ClassLoader.class, "defineClass0",
              loader,
              currAccessor,
              n,
              byteCode, 0, byteCode.length,
              domain,
              false,
              Modifier.PUBLIC,
              null
          );
          currAccessor = null;
        }
        else {
          c = defineClass(n, byteCode, 0, byteCode.length);
        }

        return c;
      }catch(IOException | ArcRuntimeException ignored1){
        return null;
      }
    });

    if (res == null)
      throw new ClassNotFoundException("no such class: " + name);

    return res;
  }

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
    Class<?> res;
    try {
      res = getParent().loadClass(name);
    }catch (ClassNotFoundException ignored){
      res = findClass(name);
    }

    if(resolve) resolveClass(res);

    return res;
  }
}
