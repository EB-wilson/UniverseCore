{
  final arc.struct.ObjectMap<String, String> bundles = arc.struct.ObjectMap.of($bundles);
  arc.files.Fi[] modsFiles = arc.Core.settings.getDataDirectory().child("mods").list();
  arc.files.Fi libFileTemp = null;
  long libVersionValue = -1;
  for (arc.files.Fi file : modsFiles) {
    if (file.isDirectory()) continue;
    arc.files.Fi zipped = new arc.files.ZipFi(file);
    arc.files.Fi modManifest = zipped.child("mod.hjson");
    if (modManifest.exists()) {
      arc.util.serialization.Jval fest = arc.util.serialization.Jval.read(modManifest.readString());
      String name = fest.get("name").asString();
      String version = fest.get("version").asString();
      if (name.equals("universe-core")) {
        libFileTemp = file;
        String[] vars = version.split("\\.");
        libVersionValue = 0;
        int priority = 10;
        for (String s : vars) {
          long base = Long.parseLong(s);
          libVersionValue += base * priority;
          priority /= 10;
        }
      }
    }
  }
  arc.files.Fi libFile = libFileTemp;
  long libVersion = libVersionValue;
  if (mindustry.Vars.mods.getMod("universe-core") == null) {
    if (libFile == null || !libFile.exists() || libVersion < $requireVersion) {
      String[] path = $className.class.getResource("").getFile().split("/");
      StringBuilder builder = new StringBuilder(path[0].replace("file:", ""));
      for(int i = 1; i < path.length; i++){
        builder.append("/").append(path[i]);
        if(path[i].contains(".jar!") || path[i].contains(".zip!")) break;
      }
      String str = arc.Core.bundle.getLocale().toString();
      String locale = (str.isEmpty() ? "bundle" : str);
      arc.util.io.PropertiesUtils.load(arc.Core.bundle.getProperties(), new java.io.StringReader(bundles.get(locale)));

      arc.util.Time.run(1, () -> {
        mindustry.ui.dialogs.BaseDialog tip = new mindustry.ui.dialogs.BaseDialog(""){
          {
            cont.table(t -> {
              t.defaults().grow();
              t.table(mindustry.gen.Tex.pane, (info) -> {
                info.defaults().padTop(4);
                if(libFile != null && libFile.exists() && libVersion < $requireVersion){
                  info.add(arc.Core.bundle.get("gen.libVersionOld")).color(arc.graphics.Color.crimson).top().padTop(10);
                }else
                  info.add(arc.Core.bundle.get("gen.libNotExist")).color(arc.graphics.Color.crimson).top().padTop(10);
                info.row();
                info.add(arc.Core.bundle.get("gen.downloadLib"));
                info.row();
                info.add(arc.Core.bundle.get("gen.downLibTip1")).color(arc.graphics.Color.gray);
                info.row();
                info.add(arc.Core.bundle.get("gen.downLibTip2")).color(arc.graphics.Color.gray).bottom().padBottom(10);
              }).height(215);
              t.row();
              t.table(buttons -> {
                buttons.defaults().grow();
                buttons.table(top -> {
                  top.defaults().grow();
                  top.button(arc.Core.bundle.get("gen.download"), () -> {
                    java.io.InputStream[] stream = new java.io.InputStream[1];
                    float[] downloadProgress = {0};
                    arc.util.Http.get("", (request) -> {
                      stream[0] = request.getResultAsStream();
                      arc.files.Fi temp = mindustry.Vars.tmpDirectory.child("Universearc.Core.jar");
                      arc.files.Fi file = mindustry.Vars.modDirectory.child("Universearc.Core.jar");
                      long length = request.getContentLength();
                      arc.func.Floatc cons = length <= 0 ? f -> {} :p -> downloadProgress[0] = p;
                      arc.util.io.Streams.copyProgress(stream[0], temp.write(false), length, 4096, cons);
                      if(libFile != null && libFile.exists()) libFile.delete();
                      temp.moveTo(file);
                      try{
                        mindustry.Vars.mods.importMod(file);
                        hide();
                        mindustry.Vars.ui.mods.show();
                      }catch(java.io.IOException e){
                        mindustry.Vars.ui.showException(e);
                        arc.util.Log.err(e);
                      }
                    }, (e) -> {
                      if(! (e instanceof java.io.IOException)){
                        StringBuilder error = new StringBuilder();
                        for(StackTraceElement ele : e.getStackTrace()){
                          error.append(ele);
                        }
                        mindustry.Vars.ui.showErrorMessage(arc.Core.bundle.get("gen.downloadFailed") + "\n" + error);
                      }
                    });
                    new mindustry.ui.dialogs.BaseDialog(""){{
                      titleTable.clearChildren();
                      cont.table(mindustry.gen.Tex.pane, (t) -> {
                        t.add(arc.Core.bundle.get("gen.downloading")).top().padTop(10).get();
                        t.row();
                        t.add(new mindustry.ui.Bar(() -> arc.util.Strings.autoFixed(downloadProgress[0], 1) + "%", () -> mindustry.graphics.Pal.accent, () -> downloadProgress[0])).growX().height(30).pad(4);
                      }).size(320, 175);
                      cont.row();
                      cont.button(arc.Core.bundle.get("gen.cancel"), () -> {
                        hide();
                        try{
                          if(stream[0] != null) stream[0].close();
                        }catch(java.io.IOException e){
                          arc.util.Log.err(e);
                        }
                      }).fill();
                    }}.show();
                  });
                  top.button(arc.Core.bundle.get("gen.openfile"), () -> {
                    mindustry.Vars.platform.showMultiFileChooser(file -> {
                      try{
                        mindustry.Vars.mods.importMod(file);
                        hide();
                        mindustry.Vars.ui.mods.show();
                      }catch(java.io.IOException e){
                        mindustry.Vars.ui.showException(e);
                        arc.util.Log.err(e);
                      }
                    }, "zip", "jar");
                  });
                  top.button(arc.Core.bundle.get("gen.goLibPage"), () -> {
                    if(! arc.Core.app.openURI("https://github.com/EB-wilson/UniverseCore")){
                      mindustry.Vars.ui.showErrorMessage("@linkfail");
                      arc.Core.app.setClipboardText("https://github.com/EB-wilson/UniverseCore");
                    }
                  });
                });

                buttons.row();
                buttons.table(bottom ->{
                  bottom.defaults().grow();
                  bottom.button(arc.Core.bundle.get("gen.openModDir"),()->{
                    if(!arc.Core.app.openFolder(mindustry.Vars.modDirectory.path())){
                      mindustry.Vars.ui.showInfo(arc.Core.bundle.get("gen.androidOpenFolder"));
                      arc.Core.app.setClipboardText(mindustry.Vars.modDirectory.path());
                    }
                  });
                  bottom.button(arc.Core.bundle.get("gen.exit"),()->arc.Core.app.exit());
                });
              }).padTop(10);
            }).height(340).width(400);
          }
        };
        tip.titleTable.clearChildren();
        tip.show();
      });

      if(libVersion == -1) $status$ = -1;
      else $status$ = libVersion;
    }
    else{
      arc.util.Log.info("dependence mod was not loaded, load it now");
      arc.util.Log.info("you will receive an exception that threw by game, tell you the UniverseCore was load fail and skipped.\ndon't worry, this is expected, it will not affect your game");
      try{
        java.lang.reflect.Method load = mindustry.mod.Mods.class.getDeclaredMethod("loadMod", arc.files.Fi.class);
        load.setAccessible(true);
        java.lang.reflect.Field f = mindustry.mod.Mods.class.getDeclaredField("mods");
        f.setAccessible(true);
        arc.struct.Seq<mindustry.mod.Mods.LoadedMod> mods = (arc.struct.Seq<mindustry.mod.Mods.LoadedMod>) f.get(mindustry.Vars.mods);
        mods.add((mindustry.mod.Mods.LoadedMod) load.invoke(mindustry.Vars.mods, libFile));
      }catch(NoSuchFieldException | NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e){
        e.printStackTrace();
      }
    }
  }

  if($status$ == 0){
    universecore.UncCore.signup($className.class);
    $cinitField$
  }
  else{
    $cinitFieldError$

    if($status$ == -1){
      arc.util.Log.err("universeCore mod file was not found");
    }
    else if($status$ >= 1){
      arc.util.Log.err("universeCore version was deprecated, version: " + $status$ + " require: $requireVersion");
    }
  }
}
