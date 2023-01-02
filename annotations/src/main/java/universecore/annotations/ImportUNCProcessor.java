package universecore.annotations;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Modifier;
import java.util.*;

@AutoService(Processor.class)
public class ImportUNCProcessor extends BaseProcessor{
  private static final String STATUS_FIELD = "$status$";

  private static final String code = """
      {
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
      
          arc.func.Intf<String> $versionValid = v -> {
            String[] $lib = v.split("\\\\.");
            String[] $req = "$requireVersion".split("\\\\.");
    
            if (Integer.parseInt($lib[0]) > Integer.parseInt($req[0])) return 2;
            for (int i = 1; i < $lib.length; i++) {
              if (Integer.parseInt($lib[i]) > Integer.parseInt($req[i])) return 0;
              if (Integer.parseInt($lib[i]) < Integer.parseInt($req[i])) return 1;
            }
            return 0;
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
      
          final boolean $upgrade = $versionValid.get($libVersion) == 1;
          final boolean $requireOld = $versionValid.get($libVersion) == 2;
          if (mindustry.Vars.mods.getMod("universe-core") == null || $upgrade || !arc.Core.settings.getBool("mod-universe-core-enabled", true)) {
            if ($libFile == null || !$libFile.exists() || $upgrade || !arc.Core.settings.getBool("mod-universe-core-enabled", true)) {
              arc.util.io.PropertiesUtils.load(arc.Core.bundle.getProperties(), new java.io.StringReader(bundles.get(arc.Core.bundle.getLocale().toString(), bundles.get(""))));
      
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
                $curr += "old$requireVersion";
                $status$ = 3;
              }
              else if ($requireOld){
                $curr += "new$requireVersion";
                $status$ = 4;
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
                                            $modStat[1].startsWith("old")? arc.Core.bundle.format("warn.uncVersionOld", $modStat[1].replace("old", "")):
                                                arc.Core.bundle.format("warn.uncVersionNewer", $modStat[1].replace("new", ""))
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
                            else if($versionValid.get($info.getString("version", "0.0.0")) == 1){
                              mindustry.Vars.ui.showErrorMessage("version was deprecated, require: $requireVersion, select: " + $info.getString("version", "0.0.0"));
                            }
                            else if($versionValid.get($info.getString("version", "0.0.0")) == 2){
                              mindustry.Vars.ui.showErrorMessage("version was too newer, require: $requireVersion, select: " + $info.getString("version", "0.0.0"));
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
              arc.util.Log.info("you will receive an exception that threw by game, tell you the UniverseCore was load fail and skipped.\\ndon't worry, this is expected, it will not affect your game");
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
          $cinitField$
        }
        else{
          $cinitFieldError$
      
          if($status$ == 1){
            arc.util.Log.err("universeCore mod was disabled");
          }
          else if($status$ == 2){
            arc.util.Log.err("universeCore mod file was not found");
          }
          else if($status$ == 3){
            arc.util.Log.err("universeCore version was deprecated, version: " + $libVersionValue + " require: $requireVersion");
          }
          else if($status$ == 4){
            arc.util.Log.err("universeCore version was too newer, version: " + $libVersionValue + " require: $requireVersion");
          }
        }
      }
      """;

  private static final HashMap<String , String> bundles = new HashMap<>();

  static {
    bundles.put("", """
    warn.uncLoadFailed = UniverseCore failed to load
    warn.uncDisabled = UniverseCore mod has been disabled
    warn.uncNotFound = UniverseCore mod file does not exist or is missing
    warn.libNotFound = NotFound
    warn.currentUncVersion = Current UniverseCore version: {0} It is recommended to install or update the latest version of UniverseCore
    warn.uncVersionOld = UniverseCore version is outdated, requires: {0}
    warn.uncVersionNewer = UniverseCore version is too newer, requires: {0}
    warn.download = Download
    warn.downloading = downloading...
    warn.downloadFailed = download failed
    warn.cancel = cancel
    warn.openfile = import from file
    warn.goLibPage = go to github
    warn.openModDir = Go to the mods directory
    warn.exit = quit
    warn.androidOpenFolder = Failed to open the directory, you can go to the following path:\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]The address has been copied to the clipboard
    warn.enableLib = enable mod
    warn.caused = The following mods cannot be loaded correctly due to abnormal Universe Core status
    """);

    bundles.put("zh_CN", """
    warn.uncLoadFailed = UniverseCore 加载失败
    warn.uncDisabled = UniverseCore mod 已被禁用
    warn.uncNotFound = UniverseCore mod 文件不存在或已丢失
    warn.libNotFound = 未找到
    warn.currentUncVersion = 当前UniverseCore版本：{0}  建议安装或更新最新版本的UniverseCore
    warn.uncVersionOld = UniverseCore 版本过旧，需要：{0}
    warn.uncVersionNewer = UniverseCore 版本太过超前，当前需要：{0}
    warn.download = 下载
    warn.downloading = 下载中...
    warn.downloadFailed = 下载失败
    warn.cancel = 取消
    warn.openfile = 从文件导入
    warn.goLibPage = 前往github
    warn.openModDir = 前往mods目录
    warn.exit = 退出
    warn.androidOpenFolder = 打开目录失败，您可前往如下路径：\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]地址已复制到剪贴板
    warn.enableLib = 启用mod
    warn.caused = 以下mod由于UniverseCore状态异常无法正确加载
    """);

    bundles.put("zh_TW", """
    warn.uncLoadFailed = UniverseCore 加載失敗
    warn.uncDisabled = UniverseCore mod 已被禁用
    warn.uncNotFound = UniverseCore mod 文件不存在或已丟失
    warn.libNotFound = 未找到
    warn.currentUncVersion = 當前UniverseCore版本：{0} 建議安裝或更新最新版本的UniverseCore
    warn.uncVersionOld = UniverseCore 版本過舊，需要：{0}
    warn.uncVersionNewer = UniverseCore 版本太過超前，當前需要：{0}
    warn.download = 下載
    warn.downloading = 下載中...
    warn.downloadFailed = 下載失敗
    warn.cancel = 取消
    warn.openfile = 從文件導入
    warn.goLibPage = 前往github
    warn.openModDir = 前往mods目錄
    warn.exit = 退出
    warn.androidOpenFolder = 打開目錄失敗，您可前往如下路徑：\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]地址已復製到剪貼板
    warn.enableLib = 啓用mod
    warn.caused = 以下mod由於Universe Core狀態異常無法正確加載
    """);

    bundles.put("ru", """
    warn.uncLoadFailed = UniverseCore не удалось загрузить
    warn.uncDisabled = Мод UniverseCore отключен.
    warn.uncNotFound = Файл мода UniverseCore не существует или отсутствует
    warn.libNotFound = Не найден
    warn.currentUncVersion = Текущая версия UniverseCore: {0} Рекомендуется установить или обновить последнюю версию UniverseCore.
    warn.uncVersionOld = Версия UniverseCore устарела, требуется: {0}
    warn.uncVersionNewer = Слишком новая версия UniverseCore, в настоящее время требуется: {0}
    warn.download = скачать
    warn.downloading = скачивание...
    warn.downloadFailed = Загрузка не удалась
    warn.cancel = Отмена
    warn.openfile = импортировать из файла
    warn.goLibPage = перейти на гитхаб
    warn.openModDir = Перейдите в каталог модов
    warn.exit = покидать
    warn.androidOpenFolder = Не удалось открыть каталог, можно перейти по следующему пути:\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]Адрес скопирован в буфер обмена
    warn.enableLib = включить моды
    warn.caused = Следующие моды не могут быть загружены правильно из-за ненормального статуса Universe Core.
    """);

    bundles.put("ja", """
    warn.uncLoadFailed = UniverseCore を読み込めませんでした
    warn.uncDisabled = UniverseCore mod が無効化されました
    warn.uncNotFound = UniverseCore mod ファイルが存在しないか、見つかりません
    warn.libNotFound = 見つかりません
    warn.currentUncVersion = 現在の UniverseCore バージョン: {0} UniverseCore の最新バージョンをインストールまたは更新することをお勧めします
    warn.uncVersionOld = UniverseCore のバージョンが古くなっています。必要なもの: {0}
    warn.uncVersionNewer = UniverseCore のバージョンが新しすぎます。現在必要なもの: {0}
    warn.download = ダウンロード
    warn.downloading = ダウンロード中...
    warn.downloadFailed = ダウンロードに失敗しました
    warn.cancel = キャンセル
    warn.openfile = ファイルからインポート
    warn.goLibPage = ギットハブに行く
    warn.openModDir = mods ディレクトリに移動します。
    warn.exit = 終了する
    warn.androidOpenFolder = ディレクトリを開けませんでした。次のパスに移動できます:\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]アドレスはクリップボードにコピーされました
    warn.enableLib = 改造を有効にする
    warn.caused = ユニバースコアの状態異常により、以下のMODが正常にロードできない
    """);
  }
  
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
    for(TypeElement anno : annotations){
      for(Element element : roundEnv.getElementsAnnotatedWith(anno)){
        Annotations.ImportUNC annotation = element.getAnnotation(Annotations.ImportUNC.class);

        String[] s = annotation.requireVersion().split("\\.");
        boolean c = s.length == 3;
        if(c){
          try {
            for (String n : s) {
              Integer.parseInt(n);
            }
          }catch (Throwable ignored){
            c = false;
          }
        }
        if (!c) throw new IllegalArgumentException("illegal version format: " + annotation.requireVersion() + ", correct format: \"x.y.z\"");

        JCTree.JCClassDecl tree = (JCTree.JCClassDecl) trees.getTree(element);
        if(!tree.sym.getSuperclass().asElement().getQualifiedName().toString().equals("mindustry.mod.Mod"))
           throw new IllegalArgumentException("import universe core require the class extend mindustry.mod.Mod");
        
        maker.at(tree);

        ArrayList<JCTree.JCExpressionStatement> init = new ArrayList<>();
        ArrayList<JCTree> vars = new ArrayList<>();

        Symbol.VarSymbol status = new Symbol.VarSymbol(
            Modifier.PRIVATE | Modifier.STATIC,
            names.fromString(STATUS_FIELD),
            symtab.byteType,
            tree.sym
        );
        tree.defs = tree.defs.prepend(maker.VarDef(status, null));

        for(JCTree def: tree.defs){
          if(def instanceof JCTree.JCVariableDecl variable){
            if((variable.mods.flags & Modifier.STATIC) != 0){
              if(variable.init != null){
                init.add(
                    maker.Exec(
                        maker.Assign(
                            maker.Ident(variable), variable.init)));
                variable.init = null;
              }
              vars.add(variable);
            }
          }
        }

        ArrayList<JCTree> tmp = new ArrayList<>(Arrays.asList(tree.defs.toArray(new JCTree[0])));
        tmp.removeIf(vars::contains);
        tree.defs = List.from(tmp);
        
        String genCode = genLoadCode(tree.sym.getQualifiedName().toString(), annotation.requireVersion(), List.from(init));

        JCTree.JCBlock 
            preLoadBody = parsers.newParser(genCode, false, false, false).block(),
            cinit = null;

        for(JCTree def: tree.defs){
          if(def instanceof JCTree.JCBlock cinitBlock){
            if(cinitBlock.isStatic()){
              cinit = cinitBlock;
            }
          }
          if(def instanceof JCTree.JCMethodDecl method){
            if(method.sym.isConstructor() && method.params.size() == 0){
              JCTree.JCClassDecl internalClass = maker.ClassDef(
                  maker.Modifiers(Modifier.PRIVATE),
                  names.fromString("INIT_INTERNAL"),
                  List.nil(),
                  null,
                  List.nil(),
                  List.of(maker.MethodDef(
                      maker.Modifiers(Modifier.PUBLIC),
                      names.init,
                      maker.TypeIdent(TypeTag.VOID),
                      List.nil(),
                      List.nil(),
                      List.nil(),
                      maker.Block(0, List.from(method.body.stats.toArray(new JCTree.JCStatement[0]))),
                      null
                  ))
              );
              tree.defs = tree.defs.append(internalClass);
              method.body = parsers.newParser("{if(" + STATUS_FIELD + " != 0) return; new INIT_INTERNAL();}", false, false, false).block();
            }
            else if(!method.sym.isConstructor()){
              method.body.stats = method.body.stats.prepend(
                  parsers.newParser("if(" + STATUS_FIELD + " != 0) return " + getDef(method.restype.type.getKind()) + ";", false, false, false).parseStatement()
              );
            }
          }
        }
        
        if(cinit == null){
          tree.defs = tree.defs.prepend(maker.Block(Flags.STATIC, preLoadBody.stats));
        }
        else{
          cinit.stats = cinit.stats.prependList(
              preLoadBody.stats
          );
        }

        tree.defs = tree.defs.prependList(List.from(vars));

        genLog(anno, tree);
      }
    }

    return super.process(annotations, roundEnv);
  }
  
  @Override
  public Set<String> getSupportedAnnotationTypes(){
    HashSet<String> annotations = new HashSet<>();
    annotations.add(Annotations.ImportUNC.class.getCanonicalName());
    return annotations;
  }
  
  private String genLoadCode(String modMain, String requireVersion, List<JCTree.JCExpressionStatement> initList){
    StringBuilder bundles = new StringBuilder();
    boolean first = true;
    for(Map.Entry<String, String> entry : ImportUNCProcessor.bundles.entrySet()){
      bundles.append(first ? "" : ", ").append("\"").append(entry.getKey()).append("\", \"").append(format(entry.getValue())).append("\"");
      first = false;
    }

    StringBuilder init = new StringBuilder();
    StringBuilder errorInit = new StringBuilder();

    for(JCTree.JCExpressionStatement state: initList){
      init.append(state);
      errorInit.append(((JCTree.JCAssign)state.expr).getVariable())
          .append(" = ")
          .append(getDef(((JCTree.JCAssign)state.expr).getVariable().type.getKind()))
          .append(";")
          .append(System.lineSeparator());
    }

    return code.replace("$bundles", bundles.toString())
        .replace("$requireVersion", requireVersion)
        .replace("$className", modMain)
        .replace("$cinitField$", init.toString())
        .replace("$cinitFieldError$", errorInit.toString());
  }

  private static String format(String input) {
    BufferedReader reader = new BufferedReader(new StringReader(input));
    StringBuilder res = new StringBuilder();
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        res.append(line).append("\\n");
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    return res.toString();
  }

  private static String getDef(TypeKind kind){
    return switch(kind){
      case VOID -> "";
      case INT, SHORT, BYTE, LONG, FLOAT, DOUBLE, CHAR -> "0";
      case BOOLEAN -> "false";
      default -> "null";
    };
  }
}
