package universecore.annotations;

import com.google.auto.service.AutoService;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.tree.JCTree;
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
              else if(child instanceof Symbol.MethodSymbol ){
                if(!inEnclosing){
                  if(!child.isConstructor()){
                    if(!inSuper){
                      addMethod(methods, (Symbol.MethodSymbol) child);
                      addMethod(absMethods, (Symbol.MethodSymbol) child);
                    }
                    if(inSuper && (child.flags()&(Modifier.PUBLIC|Modifier.PROTECTED)) != 0
                        && (child.flags()&(Modifier.FINAL|Modifier.STATIC)) == 0){
                      addMethod(absMethods, (Symbol.MethodSymbol) child);
                    }
                  }
                  else{
                    if(!inSuper){
                      addMethod(methods, (Symbol.MethodSymbol) child);
                      addMethod(absMethods, (Symbol.MethodSymbol) child);
                    }
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
  
        LinkedList<Type> queue = new LinkedList<>();
        HashMap<Type, LinkedList<Type>> interfaces = new HashMap<>();
        HashSet<String> implemented = new HashSet<>();
        assert tree.sym != null;

        for(Type type: tree.sym.getInterfaces()){
          LinkedList<Type> list = interfaces.computeIfAbsent(type, e -> new LinkedList<>());
          queue.add(type);
          list.addLast(type);
          while(!queue.isEmpty()){
            Type inter = queue.removeFirst();
            for(Type i: ((Symbol.ClassSymbol) inter.tsym).getInterfaces()){
              if(implemented.add(i.tsym.getQualifiedName().toString()) && !superInterfaces.contains(i.tsym.getQualifiedName().toString())){
                list.addLast(i);
                queue.addLast(i);
              }
            }
          }
        }

        for(LinkedList<Type> list: interfaces.values()){
          for(Type inter : list){
            for(Symbol symbol : inter.tsym.getEnclosedElements()){
              if(symbol instanceof Symbol.MethodSymbol) makeFieldEntry((Symbol.MethodSymbol) symbol);
            }
          }
        }

        for(Map.Entry<Type, LinkedList<Type>> entry: interfaces.entrySet()){
          for(int i = entry.getValue().size() - 1; i >= 0; i--){
            for(Symbol symbol : entry.getValue().get(i).tsym.getEnclosedElements()){
              if(symbol instanceof Symbol.MethodSymbol) makeMethodEntry((Symbol.MethodSymbol) symbol, entry.getKey());
            }
          }
        }

        genLog(element, tree);
      }
    }

    super.process(annoSet, roundEnvironment);
    
    return false;
  }
  
  private void makeMethodEntry(Symbol.MethodSymbol symbol, Type sourceInterface){
    AnnotationMirrors entry = getAnnotationParams(symbol, Annotations.MethodEntry.class);
    if(entry == null) return;
    for(Symbol.MethodSymbol m : handledMethods.getOrDefault(symbol.getQualifiedName().toString(), new HashSet<>())){
      if(equalOrSub(symbol, m)) return;
    }
    
    String mName = entry.getString("entryMethod");

    String[] paramTypeList = entry.getArr("paramTypes", new String[0]);
    String[] contextList = entry.getArr("context", new String[0]);
    Annotations.InsertPosition insert = entry.getEnum("insert");
    
    ParamMark[] paramMarks = new ParamMark[paramTypeList.length];
    ParamMark[] contextMarks = new ParamMark[contextList.length];
    String[] paramAssign = new String[symbol.params().size()];
    
    for(int i = 0; i < paramTypeList.length; i++){
      paramMarks[i] = new ParamMark(paramTypeList[i], false);
    }
    for(int i = 0; i < contextList.length; i++){
      contextMarks[i] = new ParamMark(contextList[i], true);
    }

    for(ParamMark mark: contextMarks){
      if(mark.bindParam != null){
        Symbol.VarSymbol[] targetParams = symbol.params().toArray(new Symbol.VarSymbol[0]);
        Symbol.VarSymbol param;
        for(int l = 0; l < targetParams.length; l++){
          param = targetParams[l];
          if(mark.bindParam.equals(param.getQualifiedName().toString())){
            FieldEntry context;
            if((context = (defaultDeclare ? absFields : fields).get(mark.context)) != null){
              paramAssign[l] = (context.inEnclose ? context.var.owner.getQualifiedName() + ".this." : "this") + "." + context.var.getQualifiedName().toString();
              break;
            }//TODO: 错误判断
          }
        }
      }
    }

    Symbol.MethodSymbol method = null;
    tag: for(Symbol.MethodSymbol m: absMethods.getOrDefault(mName, new HashSet<>())){
      Symbol.VarSymbol[] params = m.params.toArray(new Symbol.VarSymbol[0]);
      if(paramMarks.length != params.length) continue;
      for(int i = 0; i < paramMarks.length; i++){
        if(!paramMarks[i].clazz.equals(params[i].type.tsym.getQualifiedName().toString())) continue tag;
      }

      for(int i = 0; i < paramMarks.length; i++){
        ParamMark mark = paramMarks[i];
        Symbol.VarSymbol var = params[i];

        if(var != null && mark.bindParam != null){
          Symbol.VarSymbol[] targetParams = symbol.params().toArray(new Symbol.VarSymbol[0]);
          Symbol.VarSymbol param;
          for(int l = 0; l < targetParams.length; l++){
            param = targetParams[l];
            if(mark.bindParam.equals(param.getQualifiedName().toString())){
              if(mark.clazz.equals(param.type.tsym.getQualifiedName().toString())){
                paramAssign[l] = var.getQualifiedName().toString();
                break;
              }
            }
          }
        }
      }

      for(String s: paramAssign){
        if(s == null)
          throw new IllegalArgumentException("parameter assign require the parameter that annotation given equals target method parameter");
      }

      if(!defaultDeclare && !methods.get(mName).contains(m))
        throw new IllegalArgumentException("Cannot search up methods from superclass in strict mode");

      method = m;
      break;
    }

    StringBuilder parameterBuild = new StringBuilder();
    String parameter;
    if(method == null){
      for(ParamMark mark: paramMarks){
        parameterBuild.append(mark.clazz).append(", ");
      }
      parameter = parameterBuild.length() == 0 ? "" : parameterBuild.substring(0, parameterBuild.length() - 2);
      throw new NoSuchMethodError("method with name: " + mName + " and parameter: " + parameter + " was not found");
    }
    else{
      handledMethods.computeIfAbsent(symbol.getQualifiedName().toString(), k -> new HashSet<>()).add(symbol);

      for(Attribute.Compound mirror: method.getAnnotationMirrors()){
        if(mirror.getAnnotationType().toString().equals(Annotations.EntryBlocked.class.getCanonicalName())){
          Attribute.Array arr = (Attribute.Array) mirror.member(names.fromString("blockedEntries"));
          if(arr == null) return;
          for(Attribute value: arr.values){
            Attribute.Class clazz = (Attribute.Class) value;
            if(clazz.classType.tsym.getQualifiedName().equals(symbol.owner.getQualifiedName())) return;
          }
        }
      }

      for(String mark: paramAssign){
        parameterBuild.append(mark).append(", ");
      }
      parameter = parameterBuild.length() == 0 ? "" : parameterBuild.substring(0, parameterBuild.length() - 2);

      boolean existed = false;
      for(HashSet<Symbol.MethodSymbol> set: methods.values()){
        for(Symbol.MethodSymbol m: set){
          if(equalOrSub(m, symbol)) existed = true;
        }
      }

      JCTree.JCMethodDecl methodEntry;
      String callEntry = (symbol.isDefault() && (!existed || symbol.getQualifiedName().toString().equals(method.getQualifiedName().toString())) ? sourceInterface.tsym.getQualifiedName().toString() + ".super." : "") + symbol.getQualifiedName() + "(" + parameter + ");";
      
      if(!method.getEnclosingElement().equals(tree.sym)){
        StringBuilder callSuperParam = new StringBuilder();
        for(Symbol.VarSymbol param: method.params().toArray(new Symbol.VarSymbol[0])){
          callSuperParam.append(param.getQualifiedName()).append(", ");
        }
        String callSuperParameter = callSuperParam.length() == 0 ? "" : callSuperParam.substring(0, callSuperParam.length() - 2);

        JavacParser bodyParser = parsers.newParser(
            method.getReturnType().getKind() == TypeKind.VOID ? "{super." + method.getQualifiedName() + "(" + callSuperParameter + ");}" :
                "{" + method.getReturnType().tsym.getQualifiedName() + " result = super." + method.getQualifiedName() + "(" + callSuperParameter + "); return result;}", false, false, false);

        maker.at(tree);

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

        (methodEntry = maker.MethodDef(
            maker.Modifiers(method.flags()),
            method.name,
            maker.Type(method.type.getReturnType()),
            List.from(typeParameterList),
            List.from(parameterList),
            List.from(throwsList),
            bodyParser.block(),
            null
        )).setType(method.type.getReturnType());
        methodEntry.sym = new Symbol.MethodSymbol(
            method.flags(),
            method.getQualifiedName(),
            method.type,
            tree.sym
        );
        method = methodEntry.sym;

        tree.defs = tree.defs.append(methodEntry);

        addMethod(absMethods, method, true);
        addMethod(methods, method, true);
      }
      else{
        methodEntry = trees.getTree(method);
        ArrayList<JCTree.JCStatement> stats = new ArrayList<>(methodEntry.body.stats);
        JCTree.JCStatement stat;
        if(stats.size() != 0 && (stat = stats.get(stats.size() - 1)).getKind() == Tree.Kind.RETURN){
          JCTree.JCReturn r = (JCTree.JCReturn) stat;
          if(!(r.expr instanceof JCTree.JCIdent) && insert == Annotations.InsertPosition.END){
            maker.at(tree);
            JCTree.JCVariableDecl res = maker.VarDef(
                maker.Modifiers(0),
                names.fromString("$result$"),
                methodEntry.restype,
                r.expr
            );
            stats.add(stats.size() - 1, res);
            r.expr = maker.Ident(names.fromString("$result$"));

            methodEntry.body = maker.Block(0, List.from(stats));
          }
        }
      }

      JCTree.JCStatement call = parsers.newParser(callEntry, false, false, false).parseStatement();
      ArrayList<JCTree.JCStatement> stats = new ArrayList<>(methodEntry.body.stats);

      if(insert == Annotations.InsertPosition.HEAD){
        stats.add(0, call);
      }
      else{
        int index = stats.size();
        if(stats.size() != 0 && stats.get(stats.size() - 1).getKind() == Tree.Kind.RETURN){
          index--;
        }
        stats.add(index, call);
      }

      methodEntry.body = maker.Block(0, List.from(stats));
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

    String init = bind.initialize();

    maker.at(tree);
    if(!fields.containsKey(method.key) && defaultDeclare){
      FieldEntry genField = absFields.get(method.key);
      Type returnType = method.sym.getReturnType();
      if(genField == null){
        JCTree.JCExpression expr = null;
        if(!init.isEmpty()){
          expr = parsers.newParser(init, false, false, false).parseExpression();
        }

        Type rType = returnType.getKind() == TypeKind.VOID? method.sym.params().get(0).asType(): returnType;
        genField = new FieldEntry();
        JCTree.JCVariableDecl var = maker.VarDef(
            maker.Modifiers(Modifier.PUBLIC),
            names.fromString(method.key),
            maker.Type(rType),
            expr
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
      return maker.at(tree).MethodDef(
          maker.Modifiers(Modifier.PUBLIC),
          sym.name,
          maker.Type(sym.getReturnType()),
          List.nil(),
          List.nil(),
          List.nil(),
          maker.Block(0, List.of(maker.Return(field.genIdent()))),
          null
      );
    }
    
    private JCTree.JCMethodDecl genSetter(FieldEntry field){
      String paramName = sym.getParameters().get(0).name.toString();
      
      JCTree.JCVariableDecl param = maker.at(tree).VarDef(
          maker.Modifiers(Flags.PARAMETER, List.nil()),
          names.fromString(paramName),
          maker.Type(sym.getParameters().get(0).type),
          null
      );
  
      return maker.at(tree).MethodDef(
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
    String context;
    String bindParam;
    
    public ParamMark(String param, boolean context){
      String[] p = param.split("->");
      if(p.length > 2) throw new IllegalArgumentException("unexpected \"->\" amount in annotation param, param: " + param);
      if(context){
        this.context = p[0].trim();
      }
      else clazz = p[0].trim();
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
