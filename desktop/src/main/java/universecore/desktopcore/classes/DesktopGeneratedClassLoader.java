package universecore.desktopcore.classes;

import arc.Core;
import arc.files.Fi;
import arc.files.ZipFi;
import arc.struct.ObjectSet;
import universecore.util.classes.BaseGeneratedClassLoader;
import universecore.util.mods.ModInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class DesktopGeneratedClassLoader extends BaseGeneratedClassLoader{
  private static final Fi jarFileCache = Core.settings.getDataDirectory().child("universecore").child("cache");

  private final URLClassLoader fileLoader;

  private ZipFi zip;

  public DesktopGeneratedClassLoader(ModInfo mod, ClassLoader parent){
    super(mod, parent);
    zip = new ZipFi(new Fi(getFile()));
    try{
      fileLoader = new URLClassLoader(new URL[]{getFile().toURI().toURL()}, parent);
    }catch(MalformedURLException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public void declareClass(String name, byte[] byteCode){
    Fi select = null;

    String[] paths = name.split("\\.");
    for(int i = 0; i < paths.length; i++){
      select = zip.child(paths[i] + (i == paths.length - 1? ".class": ""));
    }

    assert select != null;
    if(select.exists()) return;

    try(ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(getFile()))){
      String entryName = name.replace(".", "/") + ".class";

      ObjectSet<String> added = new ObjectSet<>();

      ZipFile tempZipped = null;
      if(getFile().exists()){
        new Fi(getFile()).copyTo(jarFileCache);
        tempZipped = new ZipFile(jarFileCache.file());
      }

      ZipEntry entry = new ZipEntry(entryName);
      added.add(entry.getName());
      outputStream.putNextEntry(entry);
      outputStream.write(byteCode);
      outputStream.closeEntry();
      outputStream.flush();

      if(tempZipped == null){
        zip = new ZipFi(new Fi(getFile()));
        return;
      }
      Enumeration<? extends ZipEntry> entries = tempZipped.entries();
      while((entry = entries.nextElement()) != null){
        if(entry.isDirectory() || !added.add(entry.getName())) continue;

        outputStream.putNextEntry(new ZipEntry(entry));
        try(InputStream inputStream = tempZipped.getInputStream(entry)){
          outputStream.write(inputStream.readAllBytes());
          outputStream.closeEntry();
          outputStream.flush();
        }
      }
      zip = new ZipFi(new Fi(getFile()));
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException{
    Class<?> c = fileLoader.loadClass(name);
    if(resolve) resolveClass(c);

    return c;
  }
}
