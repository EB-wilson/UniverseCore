package universecore.androidcore.classes.dexmaker;

import com.android.dx.rop.code.BasicBlock;
import com.android.dx.rop.code.Insn;
import com.android.dx.rop.code.InsnList;
import com.android.dx.util.IntList;

import java.util.ArrayList;
import java.util.List;

public class BlockHead{
  private final List<Insn> insns = new ArrayList<>();

  private final List<BlockHead> branches = new ArrayList<>();
  private BlockHead primaryBranch;

  public boolean isAdded;

  public int labelId;

  public void addInsn(Insn insn){
    insns.add(insn);
  }

  public BasicBlock toBlock(){
    InsnList list = new InsnList(insns.size());
    IntList successors = new IntList();

    for(int i = 0; i < insns.size(); i++){
      list.set(i, insns.get(i));
    }

    for(BlockHead branch: branches){
      successors.add(branch.labelId);
    }
    successors.add(primaryBranch.labelId);

    return new BasicBlock(
        labelId,
        list,
        successors,
        primaryBranch.labelId
    );
  }
}
