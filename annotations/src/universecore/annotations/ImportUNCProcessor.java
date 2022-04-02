package universecore.annotations;

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
import java.util.regex.Pattern;

@AutoService(Processor.class)
public class ImportUNCProcessor extends BaseProcessor{
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
    
    return code.replace("$bundles", bundles.toString())
        .replace("$requireVersion", String.valueOf(requireVersion))
        .replace("$className", modMain);
  }
}
