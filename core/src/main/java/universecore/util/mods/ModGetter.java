package universecore.util.mods;

import arc.Core;
import arc.files.Fi;
import arc.files.ZipFi;
import arc.func.Boolf2;
import arc.struct.Seq;
import arc.util.serialization.Jval;
import mindustry.mod.Mod;
import universecore.util.IllegalModHandleException;

public class ModGetter{
  /**模组文件夹位置*/
  public static final Fi modDirectory = Core.settings.getDataDirectory().child("mods");

  /**传入一个文件，检查此文件是否是一个mod文件，若此文件是一个mod，若是，则返回mod的meta文件，若不是则抛出{@link IllegalModHandleException}
   * @param modFile 检查文件，可以是一个目录
   * @return 这个mod的main meta文件
   * @throws IllegalModHandleException 如果这个文件不是一个mod*/
  public static Fi checkModFormat(Fi modFile) throws IllegalModHandleException{
    try {
      if (!(modFile instanceof ZipFi) && !modFile.isDirectory()) modFile = new ZipFi(modFile);
    }
    catch (Throwable e){
      throw new IllegalModHandleException("file was not a valid zipped file");
    }

    Fi meta = modFile.child("mod.json").exists()? modFile.child("mod.json"):
        modFile.child("mod.hjson").exists()? modFile.child("mod.hjson"):
        modFile.child("plugin.json").exists()? modFile.child("plugin.json"): modFile.child("plugin.hjson");

    if(!meta.exists()) throw new IllegalModHandleException("mod format error: mod meta was not found");

    return meta;
  }

  /**判断传入的文件是否是一个mod
   * @param modFile 检查的文件
   * @return 布尔值表示的结果*/
  public static boolean isMod(Fi modFile){
    try{
      checkModFormat(modFile);
      return true;
    }catch(IllegalModHandleException e){
      return false;
    }
  }

  public static Seq<ModInfo> getModsWithFilter(Boolf2<Fi, Jval> filter){
    Seq<ModInfo> result = new Seq<>();

    for(Fi file: modDirectory.list()){
      try{
        Jval info = Jval.read(checkModFormat(file).reader());
        if(filter.get(file, info)){
          result.add(new ModInfo(file));
        }
      }catch(IllegalModHandleException ignored){}
    }

    return result;
  }

  public static Seq<ModInfo> getModsWithName(String name){
    return getModsWithFilter((f, i) -> i.getString("name").equals(name));
  }

  public static Seq<ModInfo> getModsWithClass(Class<? extends Mod> mainClass){
    return getModsWithFilter((f, i) -> i.getString("main").equals(mainClass.getCanonicalName()));
  }

  public static ModInfo getModWithName(String name){
    Seq<ModInfo> seq = getModsWithName(name);

    return seq.isEmpty()? null: seq.first();
  }

  public static ModInfo getModWithClass(Class<? extends Mod> mainClass){
    Seq<ModInfo> seq = getModsWithClass(mainClass);

    return seq.isEmpty()? null: seq.first();
  }
}
