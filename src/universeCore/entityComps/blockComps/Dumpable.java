package universeCore.entityComps.blockComps;

import arc.func.Boolf;
import arc.struct.Seq;
import mindustry.gen.Building;

public interface Dumpable extends FieldGetter{
  default byte getCdump(){
    return getField(byte.class, "cdump");
  }
  
  default void cdumpIncrease(Seq<Building> seq){
    if(this instanceof Building){
      ((Building)this).incrementDump(seq.size);
    }
  }
  
  default void cdumpIncrease(){
    cdumpIncrease(getDumps());
  }
  
  @SuppressWarnings("unchecked")
  default Seq<Building> getDumps(){
    return getField(Seq.class, "proximity");
  }
  
  /**用相应判据预测下一次执行dump操作(item, liquid, energy等)的被传输对象
   * 该操作仅预测，不改变任何变量
   * @param valid 要求判据，和dump中同一判定操作，即检查遍历到的目标是否接受这次dump*/
  default Building getDumpNext(Boolf<Building> valid){
    return getDumpNext(valid, getDumps());
  }
  
  default Building getDumpNext(Boolf<Building> valid, Seq<Building> seq){
    int dump = getCdump();
    for(int i = 0; i < seq.size; i++){
      if(!valid.get(seq.get((i + dump) % seq.size))) continue;
      return seq.get((i + dump) % seq.size);
    }
    return null;
  }
  
  /**用相应判据预测下一次执行dump操作(item, liquid, energy等)的被传输对象，并跳转下一次dump目标
   * @param valid 要求判据，和dump中同一判定操作，即检查遍历到的目标是否接受这次dump*/
  default Building getDump(Boolf<Building> valid){
    return getDump(valid, getDumps());
  }
  
  default Building getDump(Boolf<Building> valid, Seq<Building> seq){
    int dump = getCdump();
    for(int i=0; i<seq.size; i++){
      cdumpIncrease(seq);
      if(!valid.get(seq.get((i + dump) % seq.size))) continue;
      return seq.get((i + dump) % seq.size);
    }
    return null;
  }
}
