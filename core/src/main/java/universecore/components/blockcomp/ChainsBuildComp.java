package universecore.components.blockcomp;

import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Posc;
import universecore.annotations.Annotations;
import universecore.world.blocks.chains.ChainsContainer;
import universecore.world.blocks.modules.ChainsModule;

import java.util.Iterator;

/**链式方块的接口组件，这个组件提供方块连续放置时执行一些行为的能力，为一块连续结构上发生的变更添加了触发器
 *
 * @since 1.5
 * @author EBwilson*/
public interface ChainsBuildComp extends BuildCompBase, Posc, Iterable<ChainsBuildComp>{
  Seq<ChainsBuildComp> tempSeq = new Seq<>();

  /**这是个愚蠢的做法...但好像也没有什么很好的解决方式，简言之就是用于结构在存档重新装载时保持原有的结构而不发生改变*/
  @Annotations.BindField(value = "loadingInvalidPos", initialize = "new arc.struct.IntSet()")
  default IntSet loadingInvalidPos(){
    return null;
  }

  /**链式结构的容器，是连续结构保存和行为触发的核心*/
  @Annotations.BindField("chains")
  default ChainsModule chains(){
    return null;
  }
  
  default ChainsBlockComp getChainsBlock(){
    return getBlock(ChainsBlockComp.class);
  }
  
  @Annotations.MethodEntry(entryMethod = "onProximityAdded")
  default void onChainsAdded(){
    for(ChainsBuildComp other : chainBuilds()){
      if(loadingInvalidPos().contains(other.getTile().pos())) continue;
      if(canChain(other) && other.canChain(this)) other.chains().container.add(chains().container);
    }
    if(!loadingInvalidPos().isEmpty()) loadingInvalidPos().clear();
  }

  /**是否可以与目标建筑构成连续结构，只有与目标间互相均能连接时才能构成连续结构
   *
   * @param other 目标建筑*/
  default boolean canChain(ChainsBuildComp other){
    if(!getChainsBlock().chainable(other.getChainsBlock())) return false;

    return chains().container.inlerp(this, other);
  }
  
  @Annotations.MethodEntry(entryMethod = "onProximityRemoved")
  default void onChainsRemoved(){
    chains().container.remove(this);
  }

  /**获取此建筑可以连接到的其他链式方块，注意，这返回的是一个共用容器，在需要时请保存其副本*/
  default Seq<ChainsBuildComp> chainBuilds(){
    tempSeq.clear();
    for(Building other: getBuilding().proximity){
      if(other instanceof ChainsBuildComp comp && canChain(comp) && comp.canChain(this)){
        tempSeq.add((ChainsBuildComp) other);
      }
    }
    return tempSeq;
  }

  /**迭代实现，这个组件可以直接通过for-each遍历所有的结构成员*/
  @Override
  default Iterator<ChainsBuildComp> iterator(){
    return chains().container.all.iterator();
  }

  /**在这个方块创建了新的{@linkplain ChainsContainer 链式结构容器}时调用
   *
   * @param old 被替代的原容器*/
  default void containerCreated(ChainsContainer old){}

  /**在这个方块被添加到一个链式结构中时调用
   *
   * @param old 被添加到连续结构时，方块的当前容器会被目标容器取代，原有的这个被替代的原容器会从这个参数传入*/
  default void chainsAdded(ChainsContainer old){}

  /**在这个方块从{@linkplain ChainsContainer 链式结构容器}执行移除时调用
   *
   * @param children 此方块链接到的周围其他方块*/
  default void chainsRemoved(Seq<ChainsBuildComp> children){}

  /**当一个{@linkplain ChainsContainer 链式结构容器}执行遍历搜索时，其搜索并将该方块添加到容器时调用
   *
   * @param old 被添加到连续结构时，方块的当前容器会被目标容器取代，原有的这个被替代的原容器会从这个参数传入*/
  default void chainsFlowed(ChainsContainer old){}

  /**在这个连续结构任何部分发生变化<strong>之后</strong>调用*/
  default void onChainsUpdated(){}

  @Annotations.MethodEntry(entryMethod = "write", paramTypes = "arc.util.io.Writes -> write")
  default void writeChains(Writes write){
    tempSeq.clear();
    for(Building building: getBuilding().proximity){
      if(building instanceof ChainsBuildComp chain && chain.chains().container != chains().container){
        tempSeq.add(chain);
      }
    }

    write.i(tempSeq.size);
    for(ChainsBuildComp comp: tempSeq){
      write.i(comp.getTile().pos());
    }
  }

  @Annotations.MethodEntry(entryMethod = "read", paramTypes = {"arc.util.io.Reads -> read", "byte"})
  default void readChains(Reads read){
    int size = read.i();
    for(int i = 0; i < size; i++){
      loadingInvalidPos().add(read.i());
    }
  }
}
