package test;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import mindustry.mod.Mod;

public class UNCImportTest extends Mod {
  JCTree tree = new TreeMa(new Context()).Literal("abc");
  JCTree tree1 = new TreeMa(new Context()).Literal("def");
}

class TreeMa extends TreeMaker{
  public TreeMa(Context context) {
    super(context);
  }
}
