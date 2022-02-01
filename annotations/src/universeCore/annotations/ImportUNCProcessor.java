package universeCore.annotations;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@AutoService(Processor.class)
public class ImportUNCProcessor extends BaseProcessor{
  private static final String code = "{" +
      "final arc.struct.ObjectMap<String, String> bundles = arc.struct.ObjectMap.of($bundles);" +
      
      "arc.files.Fi[] modsFiles = arc.Core.settings.getDataDirectory().child(\"mods\").list();" +
      "arc.files.Fi libFileTemp = null;" +
      "long libVersionValue = -1;" +
      "for (arc.files.Fi file : modsFiles) {" +
      "  if (file.isDirectory()) continue;" +
      "  arc.files.Fi zipped = new arc.files.ZipFi(file);" +
      "  arc.files.Fi modManifest = zipped.child(\"mod.hjson\").exists() ? zipped.child(\"mod.hjson\") : zipped.child(\"mod.json\");" +
      "  if (modManifest.exists()) {" +
      "    arc.util.serialization.Jval fest = arc.util.serialization.Jval.read(modManifest.readString());" +
      "    String name = fest.get(\"name\").asString();" +
      "    String version = fest.get(\"version\").asString();" +
      "    if (name.equals(\"universe-core\")) {" +
      "      libFileTemp = file;" +
      "      String[] vars = version.split(\"\\\\.\");" +
      "      libVersionValue = 0;" +
      "      int priority = 10;" +
      "      for (String s : vars) {" +
      "        long base = Long.parseLong(s);" +
      "        libVersionValue += base * priority;" +
      "        priority /= 10;" +
      "      }" +
      "    }" +
      "  }" +
      "}" +
      "arc.files.Fi libFile = libFileTemp;" +
      "long libVersion = libVersionValue;" +
      "if (mindustry.Vars.mods.getMod(\"universe-core\") == null) {" +
      "  if (libFile == null || !libFile.exists() || libVersion < $requireVersion) {" +
      "    arc.Events.on(mindustry.game.EventType.ClientLoadEvent.class, new arc.func.Cons<>(){" +
      "      public void get(mindustry.game.EventType.ClientLoadEvent clientLoadEvent){" +
      "        String[] path = getClass().getResource(\"\").getFile().split(\"/\");" +
      "        StringBuilder builder = new StringBuilder(path[0].replace(\"file:\", \"\"));" +
      "        for(int i = 1; i < path.length; i++){" +
      "          builder.append(\"/\").append(path[i]);" +
      "          if(path[i].contains(\".jar!\") || path[i].contains(\".zip!\")) break;" +
      "        }" +
      
      "        arc.util.Time.run(0, () -> {" +
      "          String str = arc.Core.bundle.getLocale().toString();" +
      "          String locale = (str.isEmpty() ? \"bundle\" : str);" +
      "          arc.util.io.PropertiesUtils.load(arc.Core.bundle.getProperties(), new java.io.StringReader(bundles.get(locale)));" +
      
      "          mindustry.ui.dialogs.BaseDialog tip = new mindustry.ui.dialogs.BaseDialog(\"\"){" +
      "            {" +
      "              cont.table((t) -> {" +
      "                t.defaults().grow();" +
      "                t.table(mindustry.gen.Tex.pane, (info) -> {" +
      "                  info.defaults().padTop(4);" +
      "                  if(libVersion < $requireVersion){" +
      "                    info.add(arc.Core.bundle.get(\"gen.libVersionOld\")).color(arc.graphics.Color.crimson).top().padTop(10);" +
      "                  }else" +
      "                    info.add(arc.Core.bundle.get(\"gen.libNotExist\")).color(arc.graphics.Color.crimson).top().padTop(10);" +
      "                  info.row();" +
      "                  info.add(arc.Core.bundle.get(\"gen.downloadLib\"));" +
      "                  info.row();" +
      "                  info.add(arc.Core.bundle.get(\"gen.downLibTip1\")).color(arc.graphics.Color.gray);" +
      "                  info.row();" +
      "                  info.add(arc.Core.bundle.get(\"gen.downLibTip2\")).color(arc.graphics.Color.gray).bottom().padBottom(10);" +
      "                }).height(215);" +
      "                t.row();" +
      "                t.table((buttons) -> {" +
      "                  buttons.defaults().grow();" +
      "                  buttons.button(arc.Core.bundle.get(\"gen.download\"), () -> {" +
      "                    java.io.InputStream[] stream = new java.io.InputStream[1];" +
      "                    float[] downloadProgress = {0};" +
      "                    arc.util.Http.get(\"\", (request) -> {" +
      "                      stream[0] = request.getResultAsStream();" +
      "                      arc.files.Fi temp = mindustry.Vars.tmpDirectory.child(\"Universearc.Core.jar\");" +
      "                      arc.files.Fi file = mindustry.Vars.tmpDirectory.child(\"Universearc.Core.jar\");" +
      "                      long length = request.getContentLength();" +
      "                      arc.func.Floatc cons = length <= 0 ? (f) -> {" +
      "                      } : (p) -> downloadProgress[0] = p;" +
      "                      arc.util.io.Streams.copyProgress(stream[0], temp.write(false), length, 4096, cons);" +
      "                      if(libFile != null && libFile.exists()) libFile.delete();" +
      "                      temp.moveTo(file);" +
      "                      new mindustry.ui.dialogs.BaseDialog(\"\"){" +
      "                        {" +
      "                          titleTable.clearChildren();" +
      "                          cont.add(arc.Core.bundle.get(\"gen.updatedRestart\"));" +
      "                          cont.row();" +
      "                          cont.button(arc.Core.bundle.get(\"gen.sure\"), () -> arc.Core.app.exit()).fill();" +
      "                        }" +
      "                      }.show();" +
      "                    }, (e) -> {" +
      "                      if(! (e instanceof java.io.IOException)){" +
      "                        StringBuilder error = new StringBuilder();" +
      "                        for(StackTraceElement ele : e.getStackTrace()){" +
      "                          error.append(ele);" +
      "                        }" +
      "                        mindustry.Vars.ui.showErrorMessage(arc.Core.bundle.get(\"gen.downloadFailed\") + \"\\n\" + error);" +
      "                      }" +
      "                    });" +
      "                    new mindustry.ui.dialogs.BaseDialog(\"\"){" +
      "                      {" +
      "                        titleTable.clearChildren();" +
      "                        cont.table(mindustry.gen.Tex.pane, (t) -> {" +
      "                          t.add(arc.Core.bundle.get(\"gen.downloading\")).top().padTop(10).get();" +
      "                          t.row();" +
      "                          t.add(new mindustry.ui.Bar(() -> arc.util.Strings.autoFixed(downloadProgress[0], 1) + \"%\", () -> mindustry.graphics.Pal.accent, () -> downloadProgress[0])).growX().height(30).pad(4);" +
      "                        }).size(320, 175);" +
      "                        cont.row();" +
      "                        cont.button(arc.Core.bundle.get(\"gen.cancel\"), () -> {" +
      "                          hide();" +
      "                          try{" +
      "                            if(stream[0] != null) stream[0].close();" +
      "                          }catch(java.io.IOException e){" +
      "                            arc.util.Log.err(e);" +
      "                          }" +
      "                        }).fill();" +
      "                      }" +
      "                    }.show();" +
      "                  });" +
      "                  buttons.button(arc.Core.bundle.get(\"gen.goLibPage\"), () -> {" +
      "                    if(! arc.Core.app.openURI(\"https://github.com/EB-wilson/UniverseCore\")){" +
      "                      mindustry.Vars.ui.showErrorMessage(\"@linkfail\");" +
      "                      arc.Core.app.setClipboardText(\"https://github.com/EB-wilson/UniverseCore\");" +
      "                    }" +
      "                  });" +
      "                  buttons.row();" +
      "                  buttons.button(arc.Core.bundle.get(\"gen.openModDir\"), () -> {" +
      "                    if(! arc.Core.app.isAndroid()){" +
      "                      arc.Core.app.openFolder(mindustry.Vars.modDirectory.path());" +
      "                    }else{" +
      "                      mindustry.Vars.ui.showInfo(arc.Core.bundle.get(\"gen.androidOpenFolder\"));" +
      "                      arc.Core.app.setClipboardText(mindustry.Vars.modDirectory.path());" +
      "                    }" +
      "                  });" +
      "                  buttons.button(arc.Core.bundle.get(\"gen.exit\"), () -> arc.Core.app.exit());" +
      "                }).padTop(10);" +
      "              }).size(340);" +
      "            }" +
      "          };" +
      "          tip.titleTable.clearChildren();" +
      "          tip.show();" +
      "        });" +
      "      }" +
      "    });" +
      
      "    throw new RuntimeException(\"universeCore version was deprecated, version: \" + libVersion + \", require: \" + $requireVersion);" +
      "  }" +
      "  else{" +
      "    arc.util.Log.info(\"dependence mod was not loaded, load it now\");" +
      "    arc.util.Log.info(\"you will receive an exception that threw by game, tell you the UniverseCore was load fail and skipped.\\ndon\\'t worry, this is expected, it will not affect your game\");" +
      "    try{" +
      "      java.lang.reflect.Method load = mindustry.mod.Mods.class.getDeclaredMethod(\"loadMod\", arc.files.Fi.class);" +
      "      load.setAccessible(true);" +
      "      java.lang.reflect.Field f = mindustry.mod.Mods.class.getDeclaredField(\"mods\");" +
      "      f.setAccessible(true);" +
      "      arc.struct.Seq<mindustry.mod.Mods.LoadedMod> mods = (arc.struct.Seq<mindustry.mod.Mods.LoadedMod>) f.get(mindustry.Vars.mods);" +
      "      mods.add((mindustry.mod.Mods.LoadedMod) load.invoke(mindustry.Vars.mods, libFile));" +
      "    }catch(NoSuchFieldException | NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e){" +
      "      e.printStackTrace();" +
      "    }" +
      "  }" +
      "}" +
      "universeCore.UncCore.signup($className.class);" +
      "}";
  
  private final HashMap<String , String> bundles = new HashMap<>();
  
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv){
    super.init(processingEnv);
    String[] path = this.getClass().getResource("").getFile().split("/");
    StringBuilder builder = new StringBuilder(path[0].replace("file:", ""));
    
    for(int i = 1; i < path.length; ++i) {
      builder.append("/").append(path[i]);
      if (path[i].contains(".jar!") || path[i].contains(".zip!")) {
        break;
      }
    }
    File file = new File(builder.substring(0, builder.length() - 1));
    try{
      JarFile jar = new JarFile(file);
      
      Enumeration<JarEntry> entries = jar.entries();
      while(entries.hasMoreElements()){
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        if(!name.contains("bundle") || !name.contains(".properties")) continue;
     
        String[] entryName = name.split("/");
        String locate = entryName[entryName.length - 1].split("\\.")[0].replace("bundle_", "");
        StringWriter writer = new StringWriter();
        InputStream stream = jar.getInputStream(entry);
        int data;
        while((data = stream.read()) != -1){
          writer.write(data);
        }
        BufferedReader input = new BufferedReader(new StringReader(writer.toString()));
        String line;
        StringBuilder result = new StringBuilder();
        boolean b = false;
        while((line = input.readLine()) != null){
          if(b){
            result.append(line).append("\\n");
          }
          if(line.equals("#----------Generate----------#")) b = true;
        }
        bundles.put(locate, result.toString());
        System.out.println(result);
      }
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
    for(TypeElement anno : annotations){
      for(Element element : roundEnv.getElementsAnnotatedWith(anno)){
        Annotations.ImportUNC annotation = element.getAnnotation(Annotations.ImportUNC.class);
        
        JCTree.JCClassDecl tree = (JCTree.JCClassDecl) trees.getTree(element);
        if(!tree.sym.getSuperclass().asElement().getQualifiedName().toString().equals("mindustry.mod.Mod"))
           throw new IllegalArgumentException("import universe core require the class extend mindustry.mod.Mod");
        
        String genCode = genLoadCode(tree.sym.getQualifiedName().toString(), annotation.requireVersion());
        JCTree.JCBlock preLoadBody = parsers.newParser(genCode, false, false, false).block(), cinit = null;
        
        for(JCTree child: tree.defs){
          if(child instanceof JCTree.JCBlock){
            if(((JCTree.JCBlock) child).isStatic()){
              cinit = (JCTree.JCBlock) child;
            }
          }
          if(child instanceof JCTree.JCMethodDecl){
            if(((JCTree.JCMethodDecl) child).sym.isConstructor() && ((JCTree.JCMethodDecl) child).params.size() == 0){
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
                      maker.Block(0, List.from(((JCTree.JCMethodDecl) child).body.stats.toArray(new JCTree.JCStatement[0]))),
                      null
                  ))
              );
              tree.defs = tree.defs.append(internalClass);
              ((JCTree.JCMethodDecl) child).body = parsers.newParser("{new INIT_INTERNAL();}", false, false, false).block();
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
      }
    }
    
    return false;
  }
  
  @Override
  public Set<String> getSupportedAnnotationTypes(){
    HashSet<String> annotations = new HashSet<>();
    annotations.add(Annotations.ImportUNC.class.getCanonicalName());
    return annotations;
  }
  
  private String genLoadCode(String modMain, long requireVersion){
    StringBuilder bundles = new StringBuilder();
    boolean first = true;
    for(Map.Entry<String, String> entry : this.bundles.entrySet()){
      bundles.append(first ? "" : ", ").append("\"").append(entry.getKey()).append("\", \"").append(entry.getValue()).append("\"");
      first = false;
    }
    
    System.out.println(bundles);
    return code.replace("$bundles", bundles.toString())
        .replace("$requireVersion", String.valueOf(requireVersion))
        .replace("$className", modMain);
  }
}
