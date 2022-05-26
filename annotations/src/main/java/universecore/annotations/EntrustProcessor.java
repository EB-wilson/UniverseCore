package universecore.annotations;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.lang.reflect.Modifier;
import java.util.*;

@AutoService(Processor.class)
public class EntrustProcessor extends BaseProcessor{
  private static final String objField = "$entrust";
  
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
    for(TypeElement annotation : annotations){
      for(Element element : roundEnv.getElementsAnnotatedWith(annotation)){
        if(element instanceof TypeElement){
          JCTree.JCClassDecl tree = trees.getTree((TypeElement) element);
          
          maker.at(tree);
          
          boolean blackList = true;
          Type extend = Type.noType;
          HashSet<Type> interfaces = new HashSet<>();
          AnnotationMirrors anno = getAnnotationParams(tree.sym, Annotations.ImplEntries.class);
          blackList = anno.getBoolean("blackList");
          extend = anno.getType("extend");
          for(Attribute attr: anno.getArr("implement")){
            interfaces.add(((Attribute.Class) attr).classType);
          }
  
          JCTree.JCVariableDecl entrustObj = null;
          JCTree.JCTypeParameter typeParam = null;
          for(JCTree.JCTypeParameter p : tree.getTypeParameters()){
            for(JCTree.JCAnnotation jcAnnotation : p.annotations){
              if(jcAnnotation.getAnnotationType().type.tsym.getQualifiedName().toString().equals(Annotations.EntrustInst.class.getCanonicalName())){
                if(typeParam == null){
                  typeParam = p;
                }
                else throw new IllegalArgumentException("entrust should mark only one type parameter, class: " + ((TypeElement) element).getQualifiedName());
              }
            }
          }
          HashSet<String> includedInterfaces = new HashSet<>(), markInterfaces = new HashSet<>();
          for(Type i : interfaces){
            markInterfaces.add(i.tsym.getQualifiedName().toString());
          }
          HashSet<Type> implementTypes = new HashSet<>();
          LinkedList<Type> queue = new LinkedList<>();
          HashMap<String, HashSet<Symbol.MethodSymbol>> methods = new HashMap<>(), declareMethods = new HashMap<>();
          
          boolean test = false;
          Type extendType = null;
          Symbol.ClassSymbol curr = tree.sym;
          while(curr != null){
            if(curr.equals(extend.tsym)){
              test = true;
              extendType = curr.type;
            }
            if(curr == tree.sym){
              int pCount = 0;
              for(Symbol symbol : curr.getEnclosedElements()){
                if(symbol instanceof Symbol.MethodSymbol){
                  addMethod(declareMethods, (Symbol.MethodSymbol) symbol);
                  if(symbol.isConstructor()){
                    int count = 0;
                    for(Symbol.VarSymbol param : ((Symbol.MethodSymbol) symbol).getParameters()){
                      if(param.getAnnotation(Annotations.EntrustInst.class) != null){
                        count++;
                        pCount++;
                      }
                      if(count > 1)
                        throw new IllegalArgumentException("target parameter should be only one parameter marked with @Entrust annotation, class: " + ((TypeElement) element).getQualifiedName());
                    }
                  }
                }
                else if(symbol instanceof Symbol.VarSymbol){
                  if(symbol.getAnnotation(Annotations.EntrustInst.class) != null){
                    if(entrustObj == null){
                      entrustObj = (JCTree.JCVariableDecl) trees.getTree(symbol);
                    }
                    else throw new IllegalArgumentException("entrust field is require only one, class: " + ((TypeElement) element).getQualifiedName());
                  }
                }
              }
              if(pCount == 0)
                throw new IllegalArgumentException("a entrust class must have at least one constructor containing the passed in entrust instance, class: " + ((TypeElement) element).getQualifiedName());
            }
            
            for(Type inte : curr.getInterfaces()){
              if(inte.isInterface()) queue.addFirst(inte);
            }
            
            curr = (Symbol.ClassSymbol) curr.getSuperclass().tsym;
            if(curr == null || curr.getQualifiedName().equals(names.java_lang_Object)) break;
            
            for(Symbol symbol : curr.getEnclosedElements()){
              if(test && symbol instanceof Symbol.MethodSymbol && !symbol.isConstructor() && !((Symbol.MethodSymbol) symbol).isStaticOrInstanceInit()){
                if((symbol.flags() & Modifier.PUBLIC) != 0
                    && (symbol.flags() & (Modifier.STATIC | Modifier.FINAL)) == 0)
                  addMethod(methods, (Symbol.MethodSymbol) symbol);
              }
            }
          }
          
          if(!test && extend != Type.noType)
            throw new IllegalArgumentException("the annotation assigned class should equals or is super class with the class extends class, class: " + ((TypeElement) element).getQualifiedName());
          
          while(!queue.isEmpty()){
            Type inte = queue.removeLast();
            if(includedInterfaces.add(inte.tsym.getQualifiedName().toString())){
              if(interfaces.contains(inte)){
                markInterfaces.remove(inte.tsym.getQualifiedName().toString());
                implementTypes.add(inte);
                for(Symbol symbol : inte.tsym.getEnclosedElements()){
                  if(symbol instanceof Symbol.MethodSymbol){
                    if((symbol.flags() & (Modifier.STATIC | Modifier.FINAL | Modifier.PRIVATE)) == 0)
                      addMethod(methods, (Symbol.MethodSymbol) symbol);
                  }
                }
              }
              
              for(Type i : ((Symbol.ClassSymbol) inte.tsym).getInterfaces()){
                if(i.isInterface()) queue.addFirst(i);
              }
            }
          }
          if(!markInterfaces.isEmpty())
            throw new IllegalArgumentException("create entrust require the source class implement all the annotation assigned interfaces, class: " + ((TypeElement) element).getQualifiedName());
  
          assert tree.sym != null;
          ArrayList<JCTree.JCExpression> types = new ArrayList<>();
          if(extend != Type.noType && !extend.tsym.getQualifiedName().equals(names.java_lang_Object) && tree.sym.getSuperclass() != null) types.add(maker.Type(extendType));
          for(Type type: implementTypes){
            types.add(maker.Type(type));
          }
          
          if(typeParam == null){
            typeParam = maker.TypeParameter(names.fromString("$Type"), List.from(types));
            tree.typarams = tree.typarams.append(typeParam);
          }
          else{
            ArrayList<JCTree.JCExpression> typeParams = new ArrayList<>(typeParam.bounds);
            for(JCTree.JCExpression type : types){
              boolean finded = false;
              for(JCTree.JCExpression param : typeParams){
                if(type.type.equals(param.type)){
                  finded = true;
                  break;
                }
              }
              if(!finded) typeParams.add(type);
            }
            typeParam.bounds = List.from(typeParams);
          }
  
          if(entrustObj == null){
            entrustObj = maker.VarDef(
                maker.Modifiers(Modifier.PRIVATE),
                names.fromString(objField),
                maker.Ident(typeParam.name),
                null
            );
            tree.defs = tree.defs.prepend(entrustObj);
          }
          else{
            entrustObj.vartype = maker.Ident(typeParam.name);
            entrustObj.type = entrustObj.vartype.type;
            
            entrustObj.sym = new Symbol.VarSymbol(
                entrustObj.mods.flags,
                entrustObj.name,
                entrustObj.type,
                tree.sym
            );
          }
  
          for(Symbol.MethodSymbol constructor: declareMethods.getOrDefault(names.init.toString(), new HashSet<>())){
            boolean paramAssigned = false;
            JCTree.JCMethodDecl cstr = trees.getTree(constructor);
            ArrayList<JCTree.JCVariableDecl> list = new ArrayList<>(cstr.getParameters());
            JCTree.JCVariableDecl paramTarget = null;
            for(int i=0; i<list.size(); i++){
              JCTree.JCVariableDecl var = list.get(i);
              Annotations.EntrustInst inst;
              if((inst = var.sym.getAnnotation(Annotations.EntrustInst.class)) != null){
                paramAssigned = inst.value();
                list.set(i, maker.VarDef(
                    maker.Modifiers(Flags.PARAMETER, List.nil()),
                    var.name,
                    maker.Ident(typeParam.name),
                    null
                ));
                paramTarget = var;
              }
            }
            cstr.params = List.from(list);
            
            if(paramTarget == null || paramAssigned) continue;
            cstr.body = maker.Block(0, cstr.body.stats.append(
                maker.Exec(
                    maker.Assign(
                        maker.Ident(entrustObj.name),
                        maker.Ident(paramTarget.name)))));
          }
  
          for(HashSet<Symbol.MethodSymbol> set : methods.values()){
            entrust:for(Symbol.MethodSymbol method : set){
              boolean whiteValid = blackList;
              for(Symbol.MethodSymbol ms: declareMethods.getOrDefault(method.getQualifiedName().toString(), new HashSet<>())){
                if(equalOrSub(ms, method)){
                  JCTree.JCMethodDecl met = trees.getTree(ms);
                  if((met.mods.flags & Modifier.ABSTRACT) != 0 && met.body.stats.size() != 0) continue entrust;
                  
                  if(blackList){
                    continue entrust;
                  }
                  else whiteValid = true;
                }
              }
              if(!whiteValid) continue;
              
              maker.at(tree);
              
              ArrayList<JCTree.JCTypeParameter> typeParameterList = new ArrayList<>();
              ArrayList<JCTree.JCVariableDecl> parameterList = new ArrayList<>();
              ArrayList<JCTree.JCExpression> throwsList = new ArrayList<>();
  
              for(Symbol.TypeVariableSymbol typeParameter: method.getTypeParameters()){
                typeParameterList.add(maker.TypeParam(typeParameter.getQualifiedName(), (Type.TypeVar) typeParameter.type));
              }
              for(Symbol.VarSymbol param : method.params()){
                parameterList.add(maker.VarDef(maker.Modifiers(param.flags()), param.getQualifiedName(), maker.Type(param.type), null));
              }
              for(Type thrownType : method.getThrownTypes()){
                throwsList.add(maker.Type(thrownType));
              }
              
              StringBuilder paramBuilder = new StringBuilder();
              for(JCTree.JCVariableDecl param : parameterList){
                paramBuilder.append(param.name.toString()).append(", ");
              }
              String parameter = paramBuilder.length() == 0? "": paramBuilder.substring(0, paramBuilder.length() - 2);
              
              JCTree.JCMethodDecl methodDecl = maker.MethodDef(
                  maker.Modifiers(Modifier.PUBLIC),
                  method.getQualifiedName(),
                  maker.Type(method.getReturnType()),
                  List.from(typeParameterList),
                  List.from(parameterList),
                  List.from(throwsList),
                  parsers.newParser((method.getReturnType().getKind() == TypeKind.VOID? "{": "{return ") + entrustObj.name + "." + method.getQualifiedName() + "(" + parameter + ");}", false, false, false).block(),
                  null
              );
              
              tree.defs = tree.defs.append(methodDecl);
            }
          }

          genLog(annotation, tree);
        }
      }
    }

    super.process(annotations, roundEnv);
    return false;
  }
  
  @Override
  public Set<String> getSupportedAnnotationTypes(){
    HashSet<String> result = new HashSet<>();
    result.add(Annotations.Entrust.class.getCanonicalName());
    return result;
  }
}
