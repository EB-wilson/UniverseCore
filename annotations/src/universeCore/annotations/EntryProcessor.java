package universeCore.annotations;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
public class EntryProcessor extends BaseProcessor{
  @Override
  public boolean process(Set<? extends TypeElement> annoSet, RoundEnvironment roundEnvironment){
    for(TypeElement element : annoSet){
      for(Element c : roundEnvironment.getElementsAnnotatedWith(element)){
        JCTree.JCClassDecl tree = (JCTree.JCClassDecl) trees.getTree(c);
        boolean defaultDeclare = c.getAnnotation(Annotations.ImplEntries.class).value();
        HashMap<String, JCTree.JCVariableDecl> fields = new HashMap<>();
        HashMap<String, JCTree.JCVariableDecl> absFields = new HashMap<>();
        HashMap<String, HashSet<JCTree.JCMethodDecl>> methods = new HashMap<>();

        HashSet<GenMethod> getters = new HashSet<>(), setters = new HashSet<>();
        
        JCTree.JCClassDecl curr = tree;
        while(true){
          for(JCTree child: curr.defs){
            if(child instanceof JCTree.JCVariableDecl){
              Annotations.FieldKey key;
              absFields.put(((JCTree.JCVariableDecl) child).name.toString(), (JCTree.JCVariableDecl) child);
              if((key = ((JCTree.JCVariableDecl) child).sym.getAnnotation(Annotations.FieldKey.class)) != null){
                if(!fields.containsKey(key.value())) fields.put(key.value(), (JCTree.JCVariableDecl) child);
              }
              else if(defaultDeclare && !fields.containsKey(((JCTree.JCVariableDecl) child).name.toString())) fields.put(((JCTree.JCVariableDecl) child).name.toString(), (JCTree.JCVariableDecl) child);
            }
          }

          if((curr.getModifiers().flags & Modifier.STATIC) != 0 || (curr.sym.getEnclosingElement() == null || curr.sym.getEnclosingElement().getKind() == ElementKind.PACKAGE)) break;
          curr = (JCTree.JCClassDecl) trees.getTree(curr.sym.getEnclosingElement());
        }

        for(JCTree ele: tree.defs){
          if(ele instanceof JCTree.JCMethodDecl){
            Symbol.MethodSymbol method = ((JCTree.JCMethodDecl) ele).sym;
            if((method.params().size() == 0 && method.getReturnType().getKind() != TypeKind.VOID)
                || (method.params().size() == 1 && method.getReturnType().getKind() == TypeKind.VOID)){
              methods.computeIfAbsent(method.name.toString(), k -> new HashSet<>()).add((JCTree.JCMethodDecl) ele);
            }
          }
        }

        for(JCTree.JCExpression inter: tree.implementing){
          if(inter.type.isInterface()){
            Annotations.BindField bind;
            for(Symbol symbol: inter.type.tsym.getEnclosedElements()){
              if(!(symbol instanceof Symbol.MethodSymbol) || (bind = symbol.getAnnotation(Annotations.BindField.class)) == null) continue;
              GenMethod method = new GenMethod();
              method.key = bind.value();
              method.sourceMethod = (JCTree.JCMethodDecl) trees.getTree(symbol);
    
              if(!fields.containsKey(method.key) && defaultDeclare){
                
                JCTree.JCVariableDecl genField;
                genField = absFields.getOrDefault(method.key, maker.VarDef(
                    maker.Modifiers(Modifier.PUBLIC),
                    names.fromString(method.key),
                    maker.Type(method.sourceMethod.getReturnType().type),
                    null
                ));
                tree.defs.prepend(genField);
                fields.put(method.key, genField);
              }
              if(fields.containsKey(method.key)){
                if(((Symbol.MethodSymbol) symbol).getParameters().size() == 0 && ((Symbol.MethodSymbol) symbol).getReturnType().getKind() != TypeKind.VOID){
                  if(isAssignable(((Symbol.MethodSymbol) symbol).getReturnType(), fields.get(method.key).sym.asType()))
                    getters.add(method);
                }//getter
                else if(((Symbol.MethodSymbol) symbol).getParameters().size() == 1 && ((Symbol.MethodSymbol) symbol).getReturnType().getKind() == TypeKind.VOID){
                  if(isAssignable(fields.get(method.key).sym.asType(), ((Symbol.MethodSymbol) symbol).getParameters().get(0).asType()))
                    setters.add(method);
                }//setter
              }
            }
          }
        }

        tag:for(GenMethod method: setters){
          for(JCTree.JCMethodDecl m : methods.getOrDefault(method.sourceMethod.name.toString(), new HashSet<>())){
            if(m.getReturnType().type.getKind() == TypeKind.VOID && method.sourceMethod.getReturnType().type.getKind() == TypeKind.VOID && m.getParameters().size() == 1 && m.getParameters().get(0).type == method.sourceMethod.getParameters().get(0).type)
              continue tag;
          }
          JCTree.JCMethodDecl methodDecl = method.genSetter(fields.get(method.key));
          methods.computeIfAbsent(methodDecl.name.toString(), k -> new HashSet<>()).add(methodDecl);
          tree.defs = tree.defs.append(methodDecl);
        }
        
        tag:for(GenMethod method: getters){
          for(JCTree.JCMethodDecl m : methods.getOrDefault(method.sourceMethod.name.toString(), new HashSet<>())){
            if(m.getReturnType().type.getKind() != TypeKind.VOID && method.sourceMethod.getReturnType().type.getKind() != TypeKind.VOID && isAssignable(m.getReturnType().type, method.sourceMethod.getReturnType().type) && method.sourceMethod.getParameters().size() == 0 && m.getParameters().size() == 0)
              continue tag;
          }
          JCTree.JCMethodDecl methodDecl = method.genGetter(fields.get(method.key));
          methods.computeIfAbsent(methodDecl.name.toString(), k -> new HashSet<>()).add(methodDecl);
          tree.defs = tree.defs.append(methodDecl);
        }
      }
    }
    
    return false;
  }
  
  @Override
  public Set<String> getSupportedAnnotationTypes(){
    HashSet<String> result = new HashSet<>();
    result.add(Annotations.ImplEntries.class.getCanonicalName());
    return result;
  }
  
  private static boolean isAssignable(Type a, Type b){
    if(a instanceof Type.ClassType && b instanceof Type.ClassType){
      Type curr = b;
      while(curr.getKind() != TypeKind.NONE){
        if(curr.equals(a)) return true;
        curr = ((Type.ClassType)curr).supertype_field;
      }
    }
    else if(a instanceof Type.JCPrimitiveType){
      return a.equals(b);
    }
    return false;
  }
  
  private class GenMethod{
    String key;
    JCTree.JCMethodDecl sourceMethod;
  
    private JCTree.JCMethodDecl genGetter(JCTree.JCVariableDecl field){
      return maker.MethodDef(
          maker.Modifiers(Modifier.PUBLIC),
          sourceMethod.name,
          sourceMethod.restype,
          List.nil(),
          List.nil(),
          List.nil(),
          maker.Block(0, List.of(maker.Return(maker.Select(maker.QualThis(field.sym.owner.type), field.sym)))),
          null
      );
    }
    
    private JCTree.JCMethodDecl genSetter(JCTree.JCVariableDecl field){
      JCTree.JCVariableDecl param = maker.VarDef(
          maker.Modifiers(Flags.PARAMETER, List.nil()),
          sourceMethod.name,
          sourceMethod.getParameters().get(0).vartype,
          null
      );
      
      return maker.MethodDef(
          maker.Modifiers(Modifier.PUBLIC),
          sourceMethod.name,
          maker.Type(new Type.JCVoidType()),
          List.nil(),
          List.of(param),
          List.nil(),
          maker.Block(0, List.of(maker.Exec(maker.Assign(maker.Select(maker.QualThis(field.sym.owner.type), field.sym), maker.Ident(param.getName()))))),
          null
      );
    }
  }
}
