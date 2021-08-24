package universeCore.override;

import arc.files.Fi;
import arc.files.ZipFi;
import arc.struct.ObjectSet;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.mod.Mods;
import universeCore.util.handler.MethodHandler;

import static mindustry.Vars.modDirectory;
import static mindustry.Vars.platform;

public class UncMods extends Mods{
  public Seq<LoadedMod> mods = new Seq<>();
  public ObjectSet<Fi> addedMod = new ObjectSet<>();
  public Queue<ModFile> modFile = new Queue<>();
  
  @Override
  public void load(){
    Seq<Fi> allMod = new Seq<>(modDirectory.list());
    for(Fi file : allMod){
      file = new ZipFi(file);
      if(!file.extension().equals("jar") && !file.extension().equals("zip") && !(file.isDirectory() && (file.child("mod.json").exists() || file.child("mod.hjson").exists())))
        continue;
  
      Fi modManifest = file.child("mod.hjson");
  
      if(modManifest.exists()){
        addedMod.add(file);
        ModFile curr = new ModFile(file);
        Jval depends = Jval.read(modManifest.readString()).get("dependencies");
        if(depends.isArray() && depends.asArray().size > 0){
          Jval.JsonArray libRequires = depends.asArray();
          for(Jval libName : libRequires){
            Fi libMod = allMod.find(f -> {
              Fi zipped = new ZipFi(f);
              Fi Manifest = zipped.child("mod.hjson");
              if(Manifest.exists()){
                String name = Jval.read(Manifest.readString()).get("name").toString();
                return libName.asString().equals(name);
              }
              return false;
            });
            if(libMod != null){
              if(addedMod.add(libMod)) curr.lib.add(libMod);
            }
          }
        }
    
        modFile.addFirst(curr);
      }
    }
  
    for(Fi file : allMod){
      if(!addedMod.add(file)) modFile.addLast(new ModFile(file));
    }
    
    while(modFile.size > 0){
      ModFile file = modFile.removeFirst();
      if(file.lib.size > 0){
        for(Fi fi: file.lib){
          loadMod(fi);
        }
        loadMod(file.self);
      }
    }
  
    //load workshop mods now
    for(Fi file : platform.getWorkshopContent(LoadedMod.class)){
      try{
        LoadedMod mod = MethodHandler.invokeMethod(this, "loadMod", file);
        mods.add(mod);
        mod.addSteamID(file.name());
      }catch(Throwable e){
        Log.err("Failed to load mod workshop file @. Skipping.", file);
        Log.err(e);
      }
    }
  
    MethodHandler.invokeNonException(this, "resolveModState");
    MethodHandler.invokeNonException(this, "sortMods");
  
    MethodHandler.invokeNonException(this, "buildFiles");
  }
  
  private void loadMod(Fi file){
    Log.debug("[Mods] Loading mod @", file);
    try{
      LoadedMod mod = MethodHandler.invokeMethod(this, "loadMod", file);
      mods.add(mod);
    }catch(Throwable e){
      if(e instanceof ClassNotFoundException && e.getMessage().contains("mindustry.plugin.Plugin")){
        Log.info("Plugin @ is outdated and needs to be ported to 6.0! Update its main class to inherit from 'mindustry.mod.Plugin'. See https://mindustrygame.github.io/wiki/modding/6-migrationv6/");
      }
      else{
        Log.err("Failed to load mod file @. Skipping.", file);
        Log.err(e);
      }
    }
  }
  
  private static class ModFile{
    Fi self;
    Seq<Fi> lib = new Seq<>();
    
    public ModFile(Fi mod){
      self = mod;
    }
  }
}
