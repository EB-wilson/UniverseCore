package universecore.annotations;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

@AutoService(Processor.class)
public class ImportUNCProcessor extends BaseProcessor{
  private static final String STATUS_CHECKER = "checkStatus";
  private static final String STATUS_FIELD = "$status$";;

  private static final String sourceFile = "java/PreloadLibMethodTemplate.java";

  private static String code;
  
  private static final Pattern bundleMatcher = Pattern.compile("^bundles/bundle.*\\.properties$");
  
  private final HashMap<String , String> bundles = new HashMap<>();
  
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv){
    super.init(processingEnv);

    String[] path = Objects.requireNonNull(this.getClass().getResource("")).getFile().split("/");
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
        if(bundleMatcher.matcher(name).matches()){
          String[] entryName = name.split("/");
          String locate = entryName[entryName.length - 1].split("\\.")[0].replace("bundle_", "");
          StringWriter writer = new StringWriter();
          InputStream stream = jar.getInputStream(entry);
          int data;
          while((data = stream.read()) != - 1){
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
        }
        else if(name.equals(sourceFile)){
          StringWriter writer = new StringWriter();
          InputStream stream = jar.getInputStream(entry);
          int data;
          while((data = stream.read()) != - 1){
            writer.write(data);
          }
          code = writer.toString();
        }
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
    for(Map.Entry<String, String> entry : this.bundles.entrySet()){
      bundles.append(first ? "" : ", ").append("\"").append(entry.getKey()).append("\", \"").append(entry.getValue()).append("\"");
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

  private static String getDef(TypeKind kind){
    return switch(kind){
      case VOID -> "";
      case INT, SHORT, BYTE, LONG, FLOAT, DOUBLE, CHAR -> "0";
      case BOOLEAN -> "false";
      default -> "null";
    };
  }
}
