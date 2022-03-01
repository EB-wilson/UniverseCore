package universeCore.annotations;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import java.util.HashMap;
import java.util.HashSet;
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
  
  protected boolean isAssignable(Type a, Type b){
    if(a instanceof Type.ClassType && b instanceof Type.ClassType){
      return b.tsym.isSubClass(a.tsym, types);
    }
    else if(a instanceof Type.JCPrimitiveType){
      return a.equals(b);
    }
    return false;
  }
  
  protected boolean equalOrSub(Symbol.MethodSymbol m1, Symbol.MethodSymbol m2){
    Symbol.VarSymbol[] param1 = m1.params().toArray(new Symbol.VarSymbol[0]), param2 = m2.params().toArray(new Symbol.VarSymbol[0]);
    if(param1.length != param2.length) return false;
    
    for(int i = 0; i < param1.length; i++){
      if(!param1[i].type.tsym.getQualifiedName().equals(param2[i].type.tsym.getQualifiedName())) return false;
    }
    return m2.getReturnType().getKind() == m1.getReturnType().getKind()
        && (m1.getReturnType().getKind() == TypeKind.VOID
        || (isAssignable(m1.getReturnType(), m2.getReturnType()) || isAssignable(m2.getReturnType(), m1.getReturnType())));
  }
  
  protected boolean addMethod(HashMap<String, HashSet<Symbol.MethodSymbol>> map, Symbol.MethodSymbol symbol){
    HashSet<Symbol.MethodSymbol> set = map.computeIfAbsent(symbol.getQualifiedName().toString(), e -> new HashSet<>());
    for(Symbol.MethodSymbol m : set){
      if(equalOrSub(m, symbol)) return false;
    }
    set.add(symbol);
    return true;
  }
}
