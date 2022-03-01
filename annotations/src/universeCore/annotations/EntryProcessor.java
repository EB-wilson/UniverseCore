package universeCore.annotations;

import com.google.auto.service.AutoService;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.lang.reflect.Modifier;
import java.util.*;

@AutoService(Processor.class)
public class EntryProcessor extends BaseProcessor{
  JCTree.JCClassDecl tree;
  
  HashSet<String> superInterfaces = new HashSet<>();
  HashMap<String, FieldEntry> fields = new HashMap<>();
  HashMap<String, FieldEntry> absFields = new HashMap<>();
  HashMap<String, HashSet<Symbol.MethodSymbol>> methods = new HashMap<>();
  HashMap<String, HashSet<Symbol.MethodSymbol>> absMethods = new HashMap<>();
  HashMap<String, HashSet<Symbol.MethodSymbol>> handledMethods = new HashMap<>();
  HashMap<String, HashSet<Symbol.MethodSymbol>> defaultMethods = new HashMap<>();
  
  HashMap<String, GenMethod> getters = new HashMap<>(), setters = new HashMap<>();
  
  boolean defaultDeclare;
  
  @Override
  public boolean process(Set<? extends TypeElement> annoSet, RoundEnvironment roundEnvironment){
    for(TypeElement element : annoSet){
      for(Element c : roundEnvironment.getElementsAnnotatedWith(element)){
        tree = (JCTree.JCClassDecl) trees.getTree(c);
        maker.at(tree);
        defaultDeclare = c.getAnnotation(Annotations.ImplEntries.class).value();
        
        superInterfaces.clear();
        
        fields.clear();
        absFields.clear();
        methods.clear();
        absMethods.clear();
        handledMethods.clear();
        defaultMethods.clear();
        getters.clear();
        setters.clear();
        
        boolean inSuper = false, inEnclosing;
        Symbol.ClassSymbol par = tree.sym, curr;
        while(par != null){
          if(inSuper){
            if(par.getAnnotation(Annotations.ImplEntries.class) != null){
              Type currI;
              LinkedList<Type> queue = new LinkedList<>();
              queue.addFirst(par.type);
              
              while(!queue.isEmpty()){
                currI = queue.removeLast();
                for(Type inte : ((Symbol.ClassSymbol) currI.tsym).getInterfaces()){
                  if(superInterfaces.add(inte.tsym.getQualifiedName().toString())) queue.addFirst(inte);
                }
              }
            }
          }
          
          curr = par;
          inEnclosing = false;
          while(true){
            for(Symbol child : curr.getEnclosedElements()){
              if(child instanceof Symbol.VarSymbol){
                Annotations.FieldKey key = child.getAnnotation(Annotations.FieldKey.class);
                if((key != null && absFields.containsKey(key.value()))
                    || (defaultDeclare && absFields.containsKey(child.name.toString()))) continue;
                
                if(inSuper && (child.flags() & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) continue;
                
                FieldEntry field = new FieldEntry();
                field.var = (Symbol.VarSymbol) child;
                field.inSuper = inSuper;
                field.inEnclose = inEnclosing;
  
                absFields.put(child.name.toString(), field);
                if(key != null){
                  if(!fields.containsKey(key.value())) fields.put(key.value(), field);
                }else if(defaultDeclare && !fields.containsKey(child.name.toString()))
                  fields.put(child.name.toString(), field);
              }
              else if(child instanceof Symbol.MethodSymbol && !child.isConstructor()){
                if(!inEnclosing){
                  if(!inSuper){
                    addMethod(methods, (Symbol.MethodSymbol) child);
                    addMethod(absMethods, (Symbol.MethodSymbol) child);
                  }
                  if(inSuper && (child.flags() & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0
                      && (child.flags() & (Modifier.FINAL | Modifier.STATIC)) == 0){
                    addMethod(absMethods, (Symbol.MethodSymbol) child);
                  }
                }
              }
            }
    
            if((curr.flags() & Modifier.STATIC) != 0 || (curr.getEnclosingElement() == null || curr.getEnclosingElement().getKind() == ElementKind.PACKAGE))
              break;
            curr = (Symbol.ClassSymbol) curr.getEnclosingElement();
            inEnclosing = true;
          }
          par = (Symbol.ClassSymbol) par.getSuperclass().tsym;
          inSuper = true;
        }
  
        LinkedList<Type> queue = new LinkedList<>(), interfaces = new LinkedList<>();
        HashSet<String> implemented = new HashSet<>();
        assert tree.sym != null;
        queue.addFirst(tree.sym.type);
        
        while(!queue.isEmpty()){
          Type inter = queue.removeLast();
          for(Type i: ((Symbol.ClassSymbol) inter.tsym).getInterfaces()){
            if(implemented.add(i.tsym.getQualifiedName().toString()) && !superInterfaces.contains(i.tsym.getQualifiedName().toString())){
              if(i.isInterface()) interfaces.addFirst(i);
              queue.addFirst(i);
            }
          }
        }
        
        for(Type inter : interfaces){
          for(Symbol symbol : inter.tsym.getEnclosedElements()){
            if(symbol instanceof Symbol.MethodSymbol) makeFieldEntry((Symbol.MethodSymbol) symbol);
          }
        }
  
        for(int i = interfaces.size() - 1; i >= 0; i--){
          for(Symbol symbol : interfaces.get(i).tsym.getEnclosedElements()){
            if(symbol instanceof Symbol.MethodSymbol) makeMethodEntry((Symbol.MethodSymbol) symbol);
          }
        }
      }
    }
    
    return false;
  }
  
  private void makeMethodEntry(Symbol.MethodSymbol symbol){
    Annotations.MethodEntry entry = symbol.getAnnotation(Annotations.MethodEntry.class);
    if(entry == null) return;
    for(Symbol.MethodSymbol m : handledMethods.getOrDefault(symbol.getQualifiedName().toString(), new HashSet<>())){
      if(equalOrSub(symbol, m)) return;
    }
    
    String mName = entry.entryMethod();
    String[] paramTypeList = entry.paramTypes();
    
    ParamMark[] marks = new ParamMark[paramTypeList.length];
    String[] paramAssign = new String[symbol.params().size()];
    
    for(int i = 0; i < paramTypeList.length; i++){
      marks[i] = new ParamMark(paramTypeList[i]);
    }
  
    Symbol.MethodSymbol method = null;
    tag:for(Symbol.MethodSymbol m : absMethods.get(mName)){
      Symbol.VarSymbol[] params = m.params.toArray(new Symbol.VarSymbol[0]);
      if(marks.length != params.length) continue;
      for(int i = 0; i < marks.length; i++){
        if(! marks[i].clazz.equals(params[i].type.tsym.getQualifiedName().toString())) continue tag;
      }
  
      for(int i = 0; i < marks.length; i++){
        ParamMark mark = marks[i];
        Symbol.VarSymbol var = params[i];
  
        if(var != null && mark.bindParam != null){
          Symbol.VarSymbol[] targetParams = symbol.params().toArray(new Symbol.VarSymbol[0]);
          Symbol.VarSymbol param;
          for(int l = 0; l < targetParams.length; l++){
            param = targetParams[l];
            if(mark.bindParam.equals(param.getQualifiedName().toString()) && mark.clazz.equals(param.type.tsym.getQualifiedName().toString())){
              paramAssign[l] = var.getQualifiedName().toString();
              break;
            }
          }
        }
      }
  
      for(String s : paramAssign){
        if(s == null) throw new IllegalArgumentException("parameter assign require the parameter that annotation given equals target method parameter");
      }
      
      if(!defaultDeclare && !methods.get(mName).contains(m))
        throw new IllegalArgumentException("Cannot search up methods from superclass in strict mode");
      
      method = m;
      break;
    }
  
    StringBuilder parameterBuild = new StringBuilder();
    String parameter;
    if(method == null){
      for(ParamMark mark: marks){
        parameterBuild.append(mark.clazz).append(", ");
      }
      parameter = parameterBuild.length() == 0? "": parameterBuild.substring(0, parameterBuild.length() - 2);
      throw new NoSuchMethodError("method with name: " + mName + " and parameter: " + parameter + " was not found");
    }
    else{
      handledMethods.computeIfAbsent(symbol.getQualifiedName().toString(), k -> new HashSet<>()).add(symbol);
      for(String mark : paramAssign){
        parameterBuild.append(mark).append(", ");
      }
      parameter = parameterBuild.length() == 0? "": parameterBuild.substring(0, parameterBuild.length() - 2);
      
      boolean existed = false;
      for(HashSet<Symbol.MethodSymbol> set: methods.values()){
        for(Symbol.MethodSymbol m : set){
          if(equalOrSub(m, symbol)) existed = true;
        }
      }
      
      String callEntry = (symbol.isDefault() && (!existed || symbol.getQualifiedName().toString().equals(method.getQualifiedName().toString()))? symbol.owner.getQualifiedName().toString() + ".super.": "") + symbol.getQualifiedName() + "(" + parameter + ");";
      if(method.getEnclosingElement().equals(tree.sym)){
        JCTree.JCMethodDecl md = trees.getTree(method);
        JCTree.JCBlock body = md.body;
        
        JCTree.JCStatement call = parsers.newParser(callEntry, false, false, false).parseStatement();
        LinkedList<JCTree.JCStatement> stats = new LinkedList<>(body.stats);
        int index = stats.size();
        if(stats.size() != 0 && stats.getLast().getKind() == Tree.Kind.RETURN){
          index--;
        }
        stats.add(index, call);
        md.body = maker.Block(0, List.from(stats));
      }
      else{
        StringBuilder callSuperParam = new StringBuilder();
        for(Symbol.VarSymbol param : method.params().toArray(new Symbol.VarSymbol[0])){
          callSuperParam.append(param.getQualifiedName()).append(", ");
        }
        String callSuperParameter = callSuperParam.length() == 0? "": callSuperParam.substring(0, callSuperParam.length() - 2);
        
        JavacParser bodyParser = parsers.newParser(
            method.getReturnType().getKind() == TypeKind.VOID? "{super." + method.getQualifiedName() + "(" + callSuperParameter + "); " + callEntry + "}":
            "{" + method.getReturnType().tsym.getQualifiedName() + " result = super." + method.getQualifiedName() + "(" + callSuperParameter + ");" + callEntry + " return result;}", false, false, false);
        
        ArrayList<JCTree.JCTypeParameter> typeParameterList = new ArrayList<>();
        ArrayList<JCTree.JCVariableDecl> parameterList = new ArrayList<>();
        ArrayList<JCTree.JCExpression> throwsList = new ArrayList<>();
  
        for(Symbol.TypeVariableSymbol typeParameter: method.getTypeParameters()){
          typeParameterList.add(maker.TypeParam(typeParameter.getQualifiedName(), new Type.TypeVar(typeParameter.getQualifiedName(), typeParameter.owner, typeParameter.type)));
        }
        for(Symbol.VarSymbol param : method.params()){
          parameterList.add(maker.VarDef(maker.Modifiers(param.flags()), param.getQualifiedName(), maker.Type(param.type), null));
        }
        for(Type thrownType : method.getThrownTypes()){
          throwsList.add(maker.Type(thrownType));
        }
        
        JCTree.JCMethodDecl methodEntry = maker.MethodDef(
            maker.Modifiers(method.flags()),
            method.name,
            maker.Type(method.getReturnType()),
            List.from(typeParameterList),
            List.from(parameterList),
            List.from(throwsList),
            bodyParser.block(),
            null
        );
        methodEntry.sym = new Symbol.MethodSymbol(
            method.flags(),
            method.getQualifiedName(),
            method.getReturnType(),
            tree.sym
        );
        tree.defs = tree.defs.append(methodEntry);
        
        methods.computeIfAbsent(method.name.toString(), k -> new HashSet<>()).add(method);
        absMethods.computeIfAbsent(method.name.toString(), k -> new HashSet<>()).add(method);
      }
    }
  }
  
  private void makeFieldEntry(Symbol.MethodSymbol symbol){
    Annotations.BindField bind = symbol.getAnnotation(Annotations.BindField.class);
    if(symbol.isDefault() && bind == null){
      defaultMethods.computeIfAbsent(symbol.getQualifiedName().toString(), k -> new HashSet<>()).add(symbol);
    }
    if(bind == null) return;
    for(Symbol.MethodSymbol m : defaultMethods.getOrDefault(symbol.getQualifiedName().toString(), new HashSet<>())){
      if(equalOrSub(symbol, m)) return;
    }
  
    for(Symbol.MethodSymbol m : methods.getOrDefault(symbol.name.toString(), new HashSet<>())){
      if(equalOrSub(symbol, m)) return;
    }
  
    if(!symbol.isDefault()){
      throw new IllegalArgumentException("Entry require annotate on default method in interface, method: " + symbol);
    }
  
    GenMethod method = new GenMethod();
    method.key = bind.value();
    method.sym = symbol;
  
    if(!fields.containsKey(method.key) && defaultDeclare){
      FieldEntry genField = absFields.get(method.key);
      Type returnType = method.sym.getReturnType();
      if(genField == null){
        Type rType = returnType.getKind() == TypeKind.VOID? method.sym.params().get(0).asType(): returnType;
        genField = new FieldEntry();
        JCTree.JCVariableDecl var = maker.VarDef(
            maker.Modifiers(Modifier.PUBLIC),
            names.fromString(method.key),
            maker.Type(rType),
            null
        );
        tree.defs = tree.defs.prepend(var);
        genField.var = new Symbol.VarSymbol(
            var.mods.flags,
            var.name,
            rType,
            tree.sym
        );
        var.sym = genField.var;
      }
    
      fields.put(method.key, genField);
    }
    if(fields.containsKey(method.key)){
      if(symbol.getParameters().size() == 0 && symbol.getReturnType().getKind() != TypeKind.VOID){
        if(isAssignable(symbol.getReturnType(), fields.get(method.key).var.type)){
          GenMethod m;
          String name = method.sym.getQualifiedName().toString();
          if((m = getters.put(name, method)) != null && ! m.key.equals(method.key)){
            throw new IllegalArgumentException("unable to bind the same method bind to different fields or keys, method: " + m.sym);
          }
          JCTree.JCMethodDecl methodDecl = method.genGetter(fields.get(method.key));
          tree.defs = tree.defs.append(methodDecl);
        }
      }//getter
      else if(symbol.getParameters().size() == 1 && symbol.getReturnType().getKind() == TypeKind.VOID){
        if(isAssignable(fields.get(method.key).var.type, symbol.getParameters().get(0).asType())){
          GenMethod m;
          String name = method.sym.getQualifiedName().toString();
          if((m = setters.put(name, method)) != null && ! m.key.equals(method.key)){
            throw new IllegalArgumentException("unable to bind the same method bind to different fields or keys, method: " + m.sym);
          }
          if((fields.get(method.key).var.flags() & Modifier.FINAL) != 0) {
            System.out.println("[WARNING] the field with key: " + method.key + " in class: " + tree.sym.getQualifiedName() + " has modifier that final, can not create setter on final field");
            return;
          }
          JCTree.JCMethodDecl methodDecl = method.genSetter(fields.get(method.key));
          tree.defs = tree.defs.append(methodDecl);
        }
      }//setter
    }
    else throw new NoSuchFieldError("field with key: " + method.key + "was not found, this field will not be created in strict mode");
  }
  
  @Override
  public Set<String> getSupportedAnnotationTypes(){
    HashSet<String> result = new HashSet<>();
    result.add(Annotations.ImplEntries.class.getCanonicalName());
    return result;
  }
  
  private class GenMethod{
    String key;
    Symbol.MethodSymbol sym;
  
    private JCTree.JCMethodDecl genGetter(FieldEntry field){
      JCTree.JCMethodDecl method = maker.MethodDef(
          maker.Modifiers(Modifier.PUBLIC),
          sym.name,
          maker.Type(sym.getReturnType()),
          List.nil(),
          List.nil(),
          List.nil(),
          maker.Block(0, List.of(maker.Return(field.genIdent()))),
          null
      );
      
      method.accept(new TreeTranslator());
      return method;
    }
    
    private JCTree.JCMethodDecl genSetter(FieldEntry field){
      String paramName = sym.getParameters().get(0).name.toString();
      
      JCTree.JCVariableDecl param = maker.VarDef(
          maker.Modifiers(Flags.PARAMETER, List.nil()),
          names.fromString(paramName),
          maker.Type(sym.getParameters().get(0).type),
          null
      );
  
      return maker.MethodDef(
          maker.Modifiers(Modifier.PUBLIC),
          sym.name,
          maker.Type(new Type.JCVoidType()),
          List.nil(),
          List.of(param),
          List.nil(),
          maker.Block(0, List.of(maker.Exec(
              maker.Assign(
                  field.genIdent(), maker.Ident(names.fromString(paramName)))
          ))),
          null
      );
    }
  }
  
  private static class ParamMark{
    String clazz;
    String bindParam;
    
    public ParamMark(String param){
      String[] p = param.split("->");
      if(p.length > 2) throw new IllegalArgumentException("unexpected \"->\" amount in annotation param, param: " + param);
      clazz = p[0].trim();
      if(p.length == 2) bindParam = p[1].trim();
    }
  }
  
  private class FieldEntry{
    Symbol.VarSymbol var;
    boolean inSuper;
    boolean inEnclose;
    
    public JCTree.JCExpression genIdent(){
      return maker.Select(
          inEnclose? maker.Select(
              maker.Type(var.owner.type),
              new Symbol.VarSymbol(
                  16L,
                  names._this,
                  var.owner.type,
                  var.owner.type.tsym
              ))
              : maker.Ident(inSuper? names._super: names._this),
          var
      );
    }
  }
}
