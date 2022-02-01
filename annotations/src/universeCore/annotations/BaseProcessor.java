package universeCore.annotations;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import java.util.Locale;

public abstract class BaseProcessor extends AbstractProcessor{
  JavacTrees trees;
  TreeMaker maker;
  Names names;
  Types types;
  ParserFactory parsers;
  
  Elements elements;
  Filer filer;
 
  Messager messager;
  Locale locale;
  
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv){
    super.init(processingEnv);
    elements = processingEnv.getElementUtils();
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
    locale = processingEnv.getLocale();
    
    Context context = ((JavacProcessingEnvironment)processingEnv).getContext();
    trees = JavacTrees.instance(context);
    maker = TreeMaker.instance(context);
    names = Names.instance(context);
    types = Types.instance(context);
    parsers = ParserFactory.instance(context);
  }
  
  @Override
  public SourceVersion getSupportedSourceVersion(){
    return SourceVersion.latest();
  }
}
