package universecore.androidcore.classes.dexmaker;

import com.android.dx.dex.DexOptions;
import com.android.dx.dex.code.PositionList;
import com.android.dx.dex.code.RopTranslator;
import com.android.dx.dex.file.EncodedMethod;
import com.android.dx.rop.code.BasicBlockList;
import com.android.dx.rop.code.RopMethod;
import com.android.dx.rop.cst.CstMethodRef;
import com.android.dx.rop.cst.CstNat;
import com.android.dx.rop.cst.CstString;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.type.StdTypeList;
import com.android.dx.rop.type.Type;
import dynamilize.IllegalHandleException;
import dynamilize.classmaker.code.IMethod;

import java.util.ArrayList;
import java.util.List;

public class DexMethodInfo{
  private final IMethod<?, ?> methodInfo;

  BlockHead firstBlock;
  List<BlockHead> blocks = new ArrayList<>();

  public DexMethodInfo(IMethod<?, ?> methodInfo){
    this.methodInfo = methodInfo;
  }

  public String name(){
    return methodInfo.name();
  }

  public int modifiers(){
    return methodInfo.modifiers();
  }

  public void addBlock(BlockHead block){
    if(block.isAdded)
      throw new IllegalHandleException("couldn't add a block twice");

    if(firstBlock == null) firstBlock = block;
    blocks.add(block);
    block.isAdded = true;
  }

  public EncodedMethod toItem(){
    BasicBlockList list = new BasicBlockList(blocks.size());
    for(int i = 0; i < blocks.size(); i++){
      list.set(i, blocks.get(i).toBlock());
    }

    RopMethod method = new RopMethod(list, firstBlock.labelId);
    return new EncodedMethod(
        new CstMethodRef(
            new CstType(Type.intern(methodInfo.owner().realName())),
            new CstNat(new CstString(methodInfo.name()), new CstString(methodInfo.typeDescription()))
        ),
        methodInfo.modifiers(),
        RopTranslator.translate(
            method,
            PositionList.NONE,
            null,
            methodInfo.parameters().size(),
            new DexOptions()),
        new StdTypeList(0)
    );
  }

}
