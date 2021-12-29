package universeCore.internal;

import javax.annotation.processing.Processor;
import javax.tools.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UncCompiler{
  private static final DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
  
  private static final UncClassLoader classLoader = new UncClassLoader(Thread.currentThread().getContextClassLoader());
  private static final JavaCompiler nativeCompiler = ToolProvider.getSystemJavaCompiler();
  private static final UncJavaFileManager manager = new UncJavaFileManager(nativeCompiler.getStandardFileManager(collector, null, null), classLoader);
  
  public String sourceVersion = "1.8";
  public String targetVersion = "1.8";
  
  public UncCompiler(){}
  
  protected static String className(String fullClassName){
    String[] str = fullClassName.split("\\.");
    return str[str.length-1];
  }
  
  protected static String packageName(String fullClassName){
    String[] str = fullClassName.split("\\.");
    StringBuilder result = new StringBuilder(str[0]);
    for(int i=1; i<str.length-1; i++){
      result.append(".").append(str[i]);
    }
    return result.toString();
  }
  
  public ArrayList<String> options(){
    ArrayList<String> result = new ArrayList<>();
    result.add("-source");
    result.add(sourceVersion);
    result.add("-target");
    result.add(targetVersion);
    return result;
  }
  
  public Class<?> compileAndLoad(String fullClassName, String sourceCode){
    JavaStringObject compileFile = new JavaStringObject(className(fullClassName), sourceCode);
    manager.addFile(StandardLocation.SOURCE_PATH, packageName(fullClassName), className(fullClassName) + ".java", compileFile);
  
    JavaCompiler.CompilationTask task = nativeCompiler.getTask(null, manager, collector, options(), null, List.of(compileFile));
    if(!task.call()){
      for(Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()){
        System.out.println(diagnostic.getMessage(Locale.CHINESE));
        System.out.println(diagnostic.getSource().getName());
        System.out.println(diagnostic.getCode());
        System.out.println(diagnostic.getPosition());
      }
      throw new RuntimeException("compiling " + fullClassName + " failed");
    }
    try{
      return classLoader.loadClass(fullClassName);
    }catch(ClassNotFoundException e){
      throw new RuntimeException(e);
    }
  }
}
