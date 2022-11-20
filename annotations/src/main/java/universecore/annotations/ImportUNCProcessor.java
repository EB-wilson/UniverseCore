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
        final arc.struct.ObjectMap<String, String> bundles = arc.struct.ObjectMap.of($bundles);
        arc.files.Fi[] modsFiles = arc.Core.settings.getDataDirectory().child("mods").list();
        arc.files.Fi libFileTemp = null;
        arc.files.Fi modFile = null;
        String libVersionValue = "0.0.0";
        
        java.util.concurrent.atomic.AtomicBoolean disabled = new java.util.concurrent.atomic.AtomicBoolean(false);
        for (arc.files.Fi file : modsFiles) {
          if (file.isDirectory() || (!file.extension().equals("jar") && !file.extension().equals("zip"))) continue;
          
          try{
            arc.files.Fi zipped = new arc.files.ZipFi(file);
            arc.files.Fi modManifest = zipped.child("mod.hjson");
            if (modManifest.exists()) {
              arc.util.serialization.Jval fest = arc.util.serialization.Jval.read(modManifest.readString());
              String name = fest.get("name").asString();
              String version = fest.get("version").asString();
              if (name.equals("universe-core")) {
                libFileTemp = file;
                libVersionValue = version;
              }
              else if (fest.has("main") && fest.getString("main").equals($className.class.getName())){
                modFile = file;
              }
            }
          }catch(Throwable e){
            continue;
          }
          
          if (modFile != null && libFileTemp != null) break;
        }
          
        assert modFile != null;
       
        arc.func.Boolf<String> versionValid = v -> {
          String[] lib = v.split("\\\\.");
          String[] req = "$requireVersion".split("\\\\.");
          
          for (int i = 0; i < lib.length; i++) {
            if (Integer.parseInt(lib[i]) < Integer.parseInt(req[i])) return false;
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
          
        arc.files.Fi libFile = libFileTemp;
        String libVersion = libVersionValue;
        
        boolean upgrade = !versionValid.get(libVersion);
        if (mindustry.Vars.mods.getMod("universe-core") == null || upgrade || !arc.Core.settings.getBool("mod-universe-core-enabled", true)) {
          if (libFile == null || !libFile.exists() || upgrade || !arc.Core.settings.getBool("mod-universe-core-enabled", true)) {
            arc.util.io.PropertiesUtils.load(arc.Core.bundle.getProperties(), new java.io.StringReader(bundles.get(arc.Core.bundle.getLocale().toString())));
          
            String curr = arc.Core.settings.getString("unc-checkFailed", "");
            curr += modFile.path() + "::";
            if (!arc.Core.settings.getBool("mod-universe-core-enabled", true)){
              curr += "dis";
              $status$ = 1;
              disabled.set(true);
            }
            else if (libFile == null){
              curr += "none";
              $status$ = 2;
            }
            else if (upgrade){
              curr += "$requireVersion";
              $status$ = 3;
            }
            curr += ";";
          
            arc.Core.settings.put("unc-checkFailed", curr);
            if (!arc.Core.settings.getBool("unc-warningShown", false)){
              arc.Core.settings.put("unc-warningShown", true);
          
              arc.Events.on(mindustry.game.EventType.ClientLoadEvent.class, e -> {
                String modStatus = arc.Core.settings.getString("unc-checkFailed", "");
          
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
                        for (String s : modStatus.split(";")) {
                          if(s.isEmpty()) continue;
                          String[] modStat = s.split("::");
          
                          arc.files.ZipFi f = new arc.files.ZipFi(new arc.files.Fi(modStat[0]));
                          arc.files.Fi manifest = f.child("mod.json").exists()? f.child("mod.json"):
                              f.child("mod.hjson").exists()? f.child("mod.hjson"):
                              f.child("plugin.json").exists()? f.child("plugin.json"):
                              f.child("plugin.hjson");
          
                          arc.util.serialization.Jval info = arc.util.serialization.Jval.read(manifest.reader());
                          String name = info.getString("name", "");
                          String displayName = info.getString("displayName", "");
          
                          arc.files.Fi icon = f.child("icon.png");
                          table.table(modInf -> {
                            modInf.defaults().left();
                            modInf.image().size(112).get().setDrawable(icon.exists()? new arc.scene.style.TextureRegionDrawable(new arc.graphics.g2d.TextureRegion(new arc.graphics.Texture(icon))): mindustry.gen.Tex.nomap);
                            modInf.left().table(text -> {
                              text.left().defaults().left();
                              text.add("[accent]" + displayName);
                              text.row();
                              text.add("[gray]" + name);
                              text.row();
                              text.add("[crimson]" + (
                                  modStat[1].equals("dis")? arc.Core.bundle.get("warn.uncDisabled"):
                                  modStat[1].equals("none")? arc.Core.bundle.get("warn.uncNotFound"):
                                  arc.Core.bundle.format("warn.uncVersionOld", modStat[1])
                              ));
                            }).padLeft(5).top().growX();
                          }).padBottom(4).padLeft(12).padRight(12).minWidth(625).growX().fillY().left();
                          table.row();
                          table.image().color(arc.graphics.Color.gray).growX().height(6).colspan(2).pad(0).margin(0);
                        }
                      }).growY().fillX();
                    }).grow().top();
                    main.row();
                    main.image().color(mindustry.graphics.Pal.accent).growX().height(6).colspan(2).pad(0).padBottom(12).margin(0).bottom();
                    main.row();
                    main.add(arc.Core.bundle.format("warn.currentUncVersion", libFile != null? "" + libVersion: arc.Core.bundle.get("warn.libNotFound"))).padBottom(10).bottom();
                    main.row();
                    main.table(buttons -> {
                      buttons.defaults().width(160).height(55).pad(4);
                      if(disabled.get()){
                        buttons.button(arc.Core.bundle.get("warn.enableLib"), () -> {
                          arc.Core.settings.put("mod-universe-core-enabled", true);
                          mindustry.Vars.ui.showInfoOnHidden("@mods.reloadexit", () -> {
                            arc.util.Log.info("Exiting to reload mods.");
                            arc.Core.app.exit();
                          });
                        });
                      }
                      else{
                        buttons.button(arc.Core.bundle.get("warn.download"), () -> {
                          java.io.InputStream[] stream = new java.io.InputStream[1];
                          float[] downloadProgress = {0};
                          
                          mindustry.ui.dialogs.BaseDialog[] di = new mindustry.ui.dialogs.BaseDialog[]{null};
                          
                          arc.util.Http.get("https://api.github.com/repos/EB-wilson/UniverseCore/releases/latest", (res) -> {
                            arc.util.serialization.Jval json =  arc.util.serialization.Jval.read(res.getResultAsString());
                            arc.util.serialization.Jval.JsonArray assets = json.get("assets").asArray();
                          
                            arc.util.serialization.Jval asset = assets.find(j -> j.getString("name").endsWith(".jar"));
                          
                            if(asset != null){
                              String downloadUrl = asset.getString("browser_download_url");
                          
                              arc.util.Http.get(downloadUrl, result -> {
                                stream[0] = result.getResultAsStream();
                                arc.files.Fi temp = mindustry.Vars.tmpDirectory.child("UniverseCore.jar");
                                arc.files.Fi file = mindustry.Vars.modDirectory.child("UniverseCore.jar");
                                long length = result.getContentLength();
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
                                  di[0].hide();
                                }
                              }, e -> {
                                mindustry.Vars.ui.showException(arc.Core.bundle.get("warn.downloadFailed"), e);
                                arc.util.Log.err(e);
                                di[0].hide();
                              });
                            }
                            else throw new RuntimeException("release file was not found");
                          }, (e) -> {
                            mindustry.Vars.ui.showException(arc.Core.bundle.get("warn.downloadFailed"), e);
                            arc.util.Log.err(e);
                            di[0].hide();
                          });
                          di[0] = new mindustry.ui.dialogs.BaseDialog(""){{
                            titleTable.clearChildren();
                            cont.table(mindustry.gen.Tex.pane, (t) -> {
                              t.add(arc.Core.bundle.get("warn.downloading")).top().padTop(10).get();
                              t.row();
                              t.add(new mindustry.ui.Bar(() -> arc.util.Strings.autoFixed(downloadProgress[0], 1) + "%", () -> mindustry.graphics.Pal.accent, () -> downloadProgress[0])).growX().height(30).pad(4);
                            }).size(320, 175);
                            cont.row();
                            cont.button(arc.Core.bundle.get("warn.cancel"), () -> {
                              hide();
                              try{
                                if(stream[0] != null) stream[0].close();
                              }catch(java.io.IOException e){
                                arc.util.Log.err(e);
                              }
                            }).fill();
                          }};
                          di[0].show();
                        });
                        buttons.button(arc.Core.bundle.get("warn.openfile"), () -> {
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
                      }
                      buttons.button(arc.Core.bundle.get("warn.goLibPage"), () -> {
                        if(! arc.Core.app.openURI("https://github.com/EB-wilson/UniverseCore")){
                          mindustry.Vars.ui.showErrorMessage("@linkfail");
                          arc.Core.app.setClipboardText("https://github.com/EB-wilson/UniverseCore");
                        }
                      });
                      buttons.button(arc.Core.bundle.get("warn.openModDir"),()->{
                        if(!arc.Core.app.openFolder(mindustry.Vars.modDirectory.path())){
                          mindustry.Vars.ui.showInfo(arc.Core.bundle.get("warn.androidOpenFolder"));
                          arc.Core.app.setClipboardText(mindustry.Vars.modDirectory.path());
                        }
                      });
                      buttons.button(arc.Core.bundle.get("warn.exit"),()->arc.Core.app.exit());
                    }).fill().bottom().padBottom(8);
                  }).grow().top().pad(0).margin(0);
                }}.show();
              });
            }
          }
          else{
            arc.util.Log.info("dependence mod was not loaded, load it now");
            arc.util.Log.info("you will receive an exception that threw by game, tell you the UniverseCore was load fail and skipped.\\ndon't worry, this is expected, it will not affect your game");
            try{
              java.lang.reflect.Method load = mindustry.mod.Mods.class.getDeclaredMethod("loadMod", arc.files.Fi.class);
              load.setAccessible(true);
              java.lang.reflect.Field f = mindustry.mod.Mods.class.getDeclaredField("mods");
              f.setAccessible(true);
              arc.struct.Seq<mindustry.mod.Mods.LoadedMod> mods = (arc.struct.Seq<mindustry.mod.Mods.LoadedMod>) f.get(mindustry.Vars.mods);
              mods.add((mindustry.mod.Mods.LoadedMod) load.invoke(mindustry.Vars.mods, libFile));
            }catch(NoSuchFieldException | NoSuchMethodException | IllegalAccessException |
                   java.lang.reflect.InvocationTargetException e){
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
          
          if($status$ == 1){
            arc.util.Log.err("universeCore mod was disabled");
          }
          else if($status$ == 2){
            arc.util.Log.err("universeCore mod file was not found");
          }
          else if($status$ == 3){
            arc.util.Log.err("universeCore version was deprecated, version: " + libVersion + " require: $requireVersion");
          }
        }
      }
      """;

  private static final HashMap<String , String> bundles = new HashMap<>();

  static {
    bundles.put("zh_CN", """
    warn.uncLoadFailed = UniverseCore 加载失败
    warn.uncDisabled = UniverseCore mod 已被禁用
    warn.uncNotFound = UniverseCore mod 文件不存在或已丢失
    warn.libNotFound = 未找到
    warn.currentUncVersion = 当前UniverseCore版本：{0}  建议安装或更新最新版本的UniverseCore
    warn.uncVersionOld = UniverseCore 版本过旧，需要：{0}
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
