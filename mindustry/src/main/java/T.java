public class T {
  private static int $status$ = 0;

  private static final Object $bundles = null;

  static{
    String $libVersionValue = "0.0.0";
    {
      final arc.struct.ObjectMap<String, String> bundles = arc.struct.ObjectMap.of($bundles);
      arc.files.Fi[] $modsFiles = arc.Core.settings.getDataDirectory().child("mods").list();
      arc.files.Fi $libFileTemp = null;
      arc.files.Fi $modFile = null;

      java.util.concurrent.atomic.AtomicBoolean $disabled = new java.util.concurrent.atomic.AtomicBoolean(false);
      for (arc.files.Fi $file : $modsFiles) {
        if ($file.isDirectory() || (!$file.extension().equals("jar") && !$file.extension().equals("zip"))) continue;

        try{
          arc.files.Fi $zipped = new arc.files.ZipFi($file);
          arc.files.Fi $modManifest = $zipped.child("mod.hjson");
          if ($modManifest.exists()) {
            arc.util.serialization.Jval $fest = arc.util.serialization.Jval.read($modManifest.readString());
            String $name = $fest.get("name").asString();
            String $version = $fest.get("version").asString();
            if ($name.equals("universe-core")) {
              $libFileTemp = $file;
              $libVersionValue = $version;
            }
            else if ($fest.has("main") && $fest.getString("main").equals($className.class.getName())){
              $modFile = $file;
            }
          }
        }catch(Throwable e){
          continue;
        }

        if ($modFile != null && $libFileTemp != null) break;
      }

      assert $modFile != null;

      arc.func.Boolf<String> $versionValid = v -> {
        String[] $lib = v.split("\\.");
        String[] $req = "$requireVersion".split("\\.");

        for (int i = 0; i < $lib.length; i++) {
          if (Integer.parseInt($lib[i]) < Integer.parseInt($req[i])) return false;
        }
        return true;
      };
      arc.Events.on(mindustry.game.EventType.ClientLoadEvent.class, e -> {
        arc.util.Time.run(1, () -> {
          arc.Core.settings.remove("unc-checkFailed");
          arc.Core.settings.remove("unc-warningShown");
        });
      });
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        arc.Core.settings.remove("unc-checkFailed");
        arc.Core.settings.remove("unc-warningShown");
      }));

      final arc.files.Fi $libFile = $libFileTemp;
      final String $libVersion = $libVersionValue;

      final boolean $upgrade = !$versionValid.get($libVersion);
      if (mindustry.Vars.mods.getMod("universe-core") == null || $upgrade || !arc.Core.settings.getBool("mod-universe-core-enabled", true)) {
        if ($libFile == null || !$libFile.exists() || $upgrade || !arc.Core.settings.getBool("mod-universe-core-enabled", true)) {
          arc.util.io.PropertiesUtils.load(arc.Core.bundle.getProperties(), new java.io.StringReader(bundles.get(arc.Core.bundle.getLocale().toString())));

          String $curr = arc.Core.settings.getString("unc-checkFailed", "");
          $curr += $modFile.path() + "::";
          if (!arc.Core.settings.getBool("mod-universe-core-enabled", true)){
            $curr += "dis";
            $status$ = 1;
            $disabled.set(true);
          }
          else if ($libFile == null){
            $curr += "none";
            $status$ = 2;
          }
          else if ($upgrade){
            $curr += "$requireVersion";
            $status$ = 3;
          }
          $curr += ";";

          arc.Core.settings.put("unc-checkFailed", $curr);
          if (!arc.Core.settings.getBool("unc-warningShown", false)){
            arc.Core.settings.put("unc-warningShown", true);

            arc.Events.on(mindustry.game.EventType.ClientLoadEvent.class, e -> {
              String $modStatus = arc.Core.settings.getString("unc-checkFailed", "");

              new arc.scene.ui.Dialog(){{
                setFillParent(true);

                cont.table(main -> {
                  main.add(arc.Core.bundle.get("warn.uncLoadFailed"));
                  main.row();
                  main.image().color(mindustry.graphics.Pal.accent).growX().height(5).colspan(2).pad(0).padBottom(8).padTop(8).margin(0);
                  main.row();
                  main.table(t -> {
                    t.add(arc.Core.bundle.get("warn.caused")).color(arc.graphics.Color.lightGray).padBottom(10);
                    t.row();
                    t.pane(table -> {
                      for (String $s : $modStatus.split(";")) {
                        if($s.isEmpty()) continue;
                        final String[] $modStat = $s.split("::");

                        final arc.files.ZipFi $f = new arc.files.ZipFi(new arc.files.Fi($modStat[0]));
                        final arc.files.Fi manifest = $f.child("mod.json").exists()? $f.child("mod.json"):
                            $f.child("mod.hjson").exists()? $f.child("mod.hjson"):
                                $f.child("plugin.json").exists()? $f.child("plugin.json"):
                                    $f.child("plugin.hjson");

                        final arc.util.serialization.Jval $info = arc.util.serialization.Jval.read(manifest.reader());
                        final String name = $info.getString("name", "");
                        final String displayName = $info.getString("displayName", "");

                        final arc.files.Fi $icon = $f.child("icon.png");
                        table.table(modInf -> {
                          modInf.defaults().left();
                          modInf.image().size(112).get().setDrawable($icon.exists()? new arc.scene.style.TextureRegionDrawable(new arc.graphics.g2d.TextureRegion(new arc.graphics.Texture($icon))): mindustry.gen.Tex.nomap);
                          modInf.left().table(text -> {
                            text.left().defaults().left();
                            text.add("[accent]" + displayName);
                            text.row();
                            text.add("[gray]" + name);
                            text.row();
                            text.add("[crimson]" + (
                                $modStat[1].equals("dis")? arc.Core.bundle.get("warn.uncDisabled"):
                                    $modStat[1].equals("none")? arc.Core.bundle.get("warn.uncNotFound"):
                                        arc.Core.bundle.format("warn.uncVersionOld", $modStat[1])
                            ));
                          }).padLeft(5).top().growX();
                        }).padBottom(4).padLeft(12).padRight(12).growX().fillY().left();
                        table.row();
                        table.image().color(arc.graphics.Color.gray).growX().height(6).colspan(2).pad(0).margin(0);
                        table.row();
                      }
                    }).grow().maxWidth(560);
                  }).grow().top();
                  main.row();
                  main.image().color(mindustry.graphics.Pal.accent).growX().height(6).colspan(2).pad(0).padBottom(12).margin(0).bottom();
                  main.row();
                  main.add(arc.Core.bundle.format("warn.currentUncVersion", $libFile != null? "" + $libVersion: arc.Core.bundle.get("warn.libNotFound"))).padBottom(10).bottom();
                  main.row();

                  final arc.struct.Seq<arc.scene.ui.Button> $buttons = new arc.struct.Seq<>();

                  if($disabled.get()){
                    $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get("warn.enableLib"), () -> {
                      arc.Core.settings.put("mod-universe-core-enabled", true);
                      mindustry.Vars.ui.showInfoOnHidden("@mods.reloadexit", () -> {
                        arc.util.Log.info("Exiting to reload mods.");
                        arc.Core.app.exit();
                      });
                    }));
                  }
                  else{
                    $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get("warn.download"), () -> {
                      final java.io.InputStream[] $stream = new java.io.InputStream[1];
                      final float[] $downloadProgress = {0};

                      final mindustry.ui.dialogs.BaseDialog[] $di = new mindustry.ui.dialogs.BaseDialog[]{null};

                      arc.util.Http.get("https://api.github.com/repos/EB-wilson/UniverseCore/releases/latest").timeout(900).error((e) -> {
                        mindustry.Vars.ui.showException(arc.Core.bundle.get("warn.downloadFailed"), e);
                        arc.util.Log.err(e);
                        $di[0].hide();
                      }).submit((res) -> {
                        final arc.util.serialization.Jval $json =  arc.util.serialization.Jval.read(res.getResultAsString());
                        final arc.util.serialization.Jval.JsonArray $assets = $json.get("assets").asArray();

                        final arc.util.serialization.Jval $asset = $assets.find(j -> j.getString("name").endsWith(".jar"));

                        if($asset != null){
                          final String $downloadUrl = $asset.getString("browser_download_url");

                          arc.util.Http.get($downloadUrl, result -> {
                            $stream[0] = result.getResultAsStream();
                            final arc.files.Fi $temp = mindustry.Vars.tmpDirectory.child("UniverseCore.jar");
                            final arc.files.Fi $file = mindustry.Vars.modDirectory.child("UniverseCore.jar");
                            final long $length = result.getContentLength();
                            final arc.func.Floatc $cons = $length <= 0 ? f -> {} :p -> $downloadProgress[0] = p;

                            arc.util.io.Streams.copyProgress($stream[0], $temp.write(false), $length, 4096, $cons);
                            if($libFile != null && $libFile.exists()) $libFile.delete();
                            $temp.moveTo($file);
                            try{
                              mindustry.Vars.mods.importMod($file);
                              $file.file().delete();
                              hide();
                              mindustry.Vars.ui.mods.show();
                            }catch(java.io.IOException e){
                              mindustry.Vars.ui.showException(e);
                              arc.util.Log.err(e);
                              $di[0].hide();
                            }
                          }, e -> {
                            mindustry.Vars.ui.showException(arc.Core.bundle.get("warn.downloadFailed"), e);
                            arc.util.Log.err(e);
                            $di[0].hide();
                          });
                        }
                        else throw new RuntimeException("release file was not found");
                      });
                      $di[0] = new mindustry.ui.dialogs.BaseDialog(""){{
                        titleTable.clearChildren();
                        cont.table(mindustry.gen.Tex.pane, (t) -> {
                          t.add(arc.Core.bundle.get("warn.downloading")).top().padTop(10).get();
                          t.row();
                          t.add(new mindustry.ui.Bar(() -> arc.util.Strings.autoFixed($downloadProgress[0], 1) + "%", () -> mindustry.graphics.Pal.accent, () -> $downloadProgress[0])).growX().height(30).pad(4);
                        }).size(320, 175);
                        cont.row();
                        cont.button(arc.Core.bundle.get("warn.cancel"), () -> {
                          hide();
                          try{
                            if($stream[0] != null) $stream[0].close();
                          }catch(java.io.IOException e){
                            arc.util.Log.err(e);
                          }
                        }).fill();
                      }};
                      $di[0].show();
                    }));
                    $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get("warn.openfile"), () -> {
                      mindustry.Vars.platform.showMultiFileChooser(fi -> {
                        final arc.files.ZipFi $file = new arc.files.ZipFi(fi);
                        final arc.files.Fi manifest = $file.child("mod.hjson").exists()? $file.child("mod.hjson"): null;

                        if (manifest == null){
                          mindustry.Vars.ui.showErrorMessage("not a mod file, no mod.hjson found");
                          return;
                        }

                        final arc.util.serialization.Jval $info = arc.util.serialization.Jval.read(manifest.reader());

                        if (!$info.getString("name", "").equals("universe-core")){
                          mindustry.Vars.ui.showErrorMessage("not UniverseCore mod file");
                        }
                        else if(!$versionValid.get($info.getString("version", "0.0.0"))){
                          mindustry.Vars.ui.showErrorMessage("version was deprecated, require: $requireVersion, select: " + $info.getString("version", "0.0.0"));
                        }
                        else {
                          try {
                            if($libFile != null && $libFile.exists()) $libFile.delete();
                            mindustry.Vars.mods.importMod($file);
                            hide();
                            mindustry.Vars.ui.mods.show();
                          } catch (java.io.IOException e) {
                            mindustry.Vars.ui.showException(e);
                            arc.util.Log.err(e);
                          }
                        }
                      }, "zip", "jar");
                    }));
                  }
                  $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get("warn.goLibPage"), () -> {
                    if(!arc.Core.app.openURI("https://github.com/EB-wilson/UniverseCore")){
                      mindustry.Vars.ui.showErrorMessage("@linkfail");
                      arc.Core.app.setClipboardText("https://github.com/EB-wilson/UniverseCore");
                    }
                  }));
                  $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get("warn.openModDir"),()->{
                    if(!arc.Core.app.openFolder(mindustry.Vars.modDirectory.path())){
                      mindustry.Vars.ui.showInfo(arc.Core.bundle.get("warn.androidOpenFolder"));
                      arc.Core.app.setClipboardText(mindustry.Vars.modDirectory.path());
                    }
                  }));
                  $buttons.add(arc.scene.utils.Elem.newButton(arc.Core.bundle.get("warn.exit"),()->arc.Core.app.exit()));

                  main.table(buttons -> {
                    final java.util.concurrent.atomic.AtomicReference<Runnable> $rebuild = new java.util.concurrent.atomic.AtomicReference<>(() -> {
                      buttons.clearChildren();
                      if (arc.Core.scene.getWidth() < 168*($disabled.get()? 4: 5)){
                        buttons.table(but -> {
                          but.defaults().growX().height(55).pad(4);
                          for (arc.scene.ui.Button button : $buttons) {
                            but.add(button);
                            but.row();
                          }
                        }).growX().fillY();
                      }
                      else{
                        buttons.table(but -> {
                          but.defaults().width(160).height(55).pad(4);
                          for (arc.scene.ui.Button button : $buttons) {
                            but.add(button);
                          }
                        }).fill().bottom().padBottom(8);
                      }
                    });

                    $rebuild.get();
                    arc.Events.on(mindustry.game.EventType.ResizeEvent.class, e -> {
                      $rebuild.get().run();
                    });
                  }).growX().fillY();
                }).grow().top().pad(0).margin(0);
              }}.show();
            });
          }
        }
        else{
          arc.util.Log.info("dependence mod was not loaded, load it now");
          arc.util.Log.info("you will receive an exception that threw by game, tell you the UniverseCore was load fail and skipped.\ndon't worry, this is expected, it will not affect your game");
          try{
            java.lang.reflect.Method $load = mindustry.mod.Mods.class.getDeclaredMethod("loadMod", arc.files.Fi.class);
            $load.setAccessible(true);
            java.lang.reflect.Field $f = mindustry.mod.Mods.class.getDeclaredField("mods");
            $f.setAccessible(true);
            arc.struct.Seq<mindustry.mod.Mods.LoadedMod> mods = (arc.struct.Seq<mindustry.mod.Mods.LoadedMod>) $f.get(mindustry.Vars.mods);
            mods.add((mindustry.mod.Mods.LoadedMod) $load.invoke(mindustry.Vars.mods, $libFile));
          }catch(NoSuchFieldException | NoSuchMethodException | IllegalAccessException |
                 java.lang.reflect.InvocationTargetException e){
            e.printStackTrace();
          }
        }
      }
    }

    if($status$ == 0){
      universecore.UncCore.signup($className.class);
      //$cinitField$
    }
    else{
      //$cinitFieldError$

      if($status$ == 1){
        arc.util.Log.err("universeCore mod was disabled");
      }
      else if($status$ == 2){
        arc.util.Log.err("universeCore mod file was not found");
      }
      else if($status$ == 3){
        arc.util.Log.err("universeCore version was deprecated, version: " + $libVersionValue + " require: $requireVersion");
      }
    }
  }
}
