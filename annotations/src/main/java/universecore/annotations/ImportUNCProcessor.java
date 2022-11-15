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
              String[] vars = version.split("\\\\.");
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
                              mindustry.Vars.ui.showErrorMessage(arc.Core.bundle.get("gen.downloadFailed") + "\\n" + error);
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
            arc.util.Log.info("you will receive an exception that threw by game, tell you the UniverseCore was load fail and skipped.\\ndon't worry, this is expected, it will not affect your game");
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
      """;
  
  private static final HashMap<String , String> bundles = new HashMap<>();

  static {
    bundles.put("zh_CN", """
    gen.sure = 确定
    gen.download = 下载
    gen.openfile = 从文件导入
    gen.exit = 退出
    gen.cancel = 取消
    gen.downloading = 下载中...
    gen.updatedRestart = 前置已安装，请重启游戏
    gen.libVersionOld = Universe Core版本过旧
    gen.goLibPage = 前往github
    gen.libNotExist = UniverseCore未安装或文件已丢失
    gen.downloadLib = 请安装或更新最新版本的UniverseCore
    gen.downLibTip1 = 您也可能同时安装了新旧版本的前置，可前往mods文件夹将旧版本删除
    gen.downLibTip2 = 如果您已经下载了mod文件，那么您可以将它放入mods文件夹
    gen.androidOpenFolder = 打开目录失败，您可前往如下路径：\\n[accent]android/data/io.anuke.mindustry/mods[]\\n[gray]地址已复制到剪贴板
    gen.openModDir = 前往mods目录
    """);
  }
  
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
    for(TypeElement anno : annotations){
      for(Element element : roundEnv.getElementsAnnotatedWith(anno)){
        Annotations.ImportUNC annotation = element.getAnnotation(Annotations.ImportUNC.class);
        
        JCTree.JCClassDecl tree = (JCTree.JCClassDecl) trees.getTree(element);
        if(!tree.sym.getSuperclass().asElement().getQualifiedName().toString().equals("mindustry.mod.Mod"))
           throw new IllegalArgumentException("import universe core require the class extend mindustry.mod.Mod");
        
        maker.at(tree);

        ArrayList<JCTree.JCExpressionStatement> init = new ArrayList<>();
        ArrayList<JCTree> vars = new ArrayList<>();

        Symbol.VarSymbol status = new Symbol.VarSymbol(
            Modifier.PRIVATE | Modifier.STATIC,
            names.fromString(STATUS_FIELD),
            symtab.longType,
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
        System.out.println(genCode);
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
  
  private String genLoadCode(String modMain, long requireVersion, List<JCTree.JCExpressionStatement> initList){
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
        .replace("$requireVersion", String.valueOf(requireVersion))
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
