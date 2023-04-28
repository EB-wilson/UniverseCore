package universecore.annotations;

import com.google.auto.service.AutoService;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
public class InstanceOptimizeProcessor extends BaseProcessor{
  ArrayList<JCTree.JCBlock> currHandles = new ArrayList<>();

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    for (TypeElement anno : set) {
      for (Element element : roundEnvironment.getElementsAnnotatedWith(anno)) {
        JCTree tree = trees.getTree(element);

        if (element.getKind() == ElementKind.CLASS){
          t: for (List<JCTree> c = ((JCTree.JCClassDecl) tree).defs; c != null; c = c.tail) {
            JCTree def = c.head;
            if (def instanceof JCTree.JCVariableDecl){
              JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) def;

              if (field.init instanceof JCTree.JCNewClass){
                for (JCTree d : ((JCTree.JCNewClass) field.init).def.defs) {
                  if (!(d instanceof JCTree.JCBlock)) continue t;
                }

                for (JCTree d : ((JCTree.JCNewClass) field.init).def.defs) {
                  currHandles.add((JCTree.JCBlock) d);
                }

                ((JCTree.JCNewClass) field.init).def = null;

                List<JCTree> t = c.tail;
                ArrayList<JCTree.JCStatement> stats = new ArrayList<>();
                for (JCTree.JCBlock currHandle : currHandles) {
                  currHandle.accept(new TreeScanner(){
                    @Override
                    public void visitIdent(JCTree.JCIdent tree) {
                      if (tree.sym.name.equals(names._this) && tree.sym.getEnclosingElement().equals(element)){
                        tree.sym = ((JCTree.JCVariableDecl) def).sym;
                        tree.name = tree.sym.name;
                      }
                      super.visitIdent(tree);
                    }
                  });

                  stats.add(currHandle);
                }
                currHandles.clear();
                c.append(maker.Block(0, List.from(stats))).tail = t;
              }
            }
          }

          genLog(anno, (JCTree.JCClassDecl) tree);
        }
        else if (element.getKind() == ElementKind.METHOD){
          JCTree.JCBlock block = ((JCTree.JCMethodDecl) element).body;
          block.accept(new TreeScanner(){
            int stat = -1;

            @Override
            public void visitAssign(JCTree.JCAssign tree) {
              super.visitAssign(tree);

              if (stat == 0){

              }
            }

            @Override
            public void visitNewClass(JCTree.JCNewClass tree) {
              for (JCTree d : tree.def.defs) {
                if (!(d instanceof JCTree.JCBlock)) return;
              }

              for (JCTree d : tree.def.defs) {
                currHandles.add((JCTree.JCBlock) d);
              }

              tree.def = null;
              super.visitNewClass(tree);

              stat = 0;
            }
          });

          genLog(anno, (JCTree.JCClassDecl) trees.getTree(element.getEnclosingElement()));
        }
      }
    }

    return super.process(set, roundEnvironment);
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    HashSet<String> result = new HashSet<>();
    result.add(Annotations.InstanceOptimize.class.getCanonicalName());
    return result;
  }
}
