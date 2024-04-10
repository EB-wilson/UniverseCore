package universecore.world.blocks.chains;

import arc.Core;
import arc.func.Boolf;
import arc.struct.ObjectSet;
import arc.struct.OrderedSet;
import arc.struct.Queue;
import arc.struct.Seq;
import universecore.annotations.Annotations;
import universecore.components.ExtraVariableComp;
import universecore.components.blockcomp.ChainsBuildComp;

import static mindustry.Vars.tilesize;

@Annotations.ImplEntries
public class ChainsContainer implements ExtraVariableComp {
  private static final Queue<ChainsBuildComp> findQueue = new Queue<>();
  private static final ObjectSet<ChainsBuildComp> added = new ObjectSet<>();
  
  public final OrderedSet<ChainsBuildComp> all = new OrderedSet<>();

  private int minX, minY;
  private int maxX, maxY;
  
  private long lastFrameUpdated;
  private boolean structUpdated = true;

  public boolean inlerp(ChainsBuildComp origin, ChainsBuildComp other){
    if(!all.contains(origin)) return false;
    ChainsContainer otherContainer = other.chains().container;

    return Math.max(maxX(), otherContainer.maxX()) - Math.min(minX(), otherContainer.minX()) < Math.min(origin.getChainsBlock().maxWidth(), other.getChainsBlock().maxWidth())
        && Math.max(maxY(), otherContainer.maxY()) - Math.min(minY(), otherContainer.minY()) < Math.min(origin.getChainsBlock().maxHeight(), other.getChainsBlock().maxHeight());
  }

  public int minX(){
    return minX;
  }

  public int maxX(){
    return maxX;
  }

  public int minY(){
    return minY;
  }

  public int maxY(){
    return maxY;
  }

  public int width(){
    return maxX - minX + 1;
  }

  public int height(){
    return maxY - minY + 1;
  }

  /**@deprecated 请使用setVar(String, Object)
   * @see ChainsContainer#setVar(String, Object) */
  @Deprecated
  public void putVar(String key, Object obj){
    setVar(key, obj);
  }

  public void add(ChainsContainer other){
    for(ChainsBuildComp next : other.all){
      add(next);
    }
  }

  private void updateEdge(ChainsBuildComp other){
    float offset = other.getBlock().size/2f, centOffset = other.getBlock().offset/tilesize;
    if(all.isEmpty()){
      minX = (int) Math.ceil(other.tileX() + centOffset - offset);
      minY = (int) Math.ceil(other.tileY() + centOffset - offset);
      maxX = (int) Math.floor(other.tileX() + centOffset + offset);
      maxY = (int) Math.floor(other.tileY() + centOffset + offset);
    }
    else{
      minX = Math.min(minX, (int) Math.ceil(other.tileX() + centOffset - offset));
      minY = Math.min(minY, (int) Math.ceil(other.tileY() + centOffset - offset));
      maxX = Math.max(maxX, (int) Math.floor(other.tileX() + centOffset + offset));
      maxY = Math.max(maxY, (int) Math.floor(other.tileY() + centOffset + offset));
    }
  }
  
  public void add(ChainsBuildComp other){
    if(all.contains(other)) return;

    updateEdge(other);
    all.add(other);

    ChainsContainer oldContainer = other.chains().container;
    other.chains().container = this;
    
    other.chainsAdded(oldContainer);

    structUpdated = true;
  }
  
  public void update(){
    if(Core.graphics.getFrameId() == lastFrameUpdated) return;
    lastFrameUpdated = Core.graphics.getFrameId();

    if(structUpdated){
      for(ChainsBuildComp comp: all){
        comp.onChainsUpdated();
      }

      structUpdated = false;
    }
  }
  
  public void reconstruct(ChainsBuildComp source, Boolf<ChainsBuildComp> filter){
    findQueue.clear();
    added.clear();
    
    findQueue.addFirst(source);
    added.add(source);
    while(!findQueue.isEmpty()){
      ChainsBuildComp other = findQueue.removeLast();
      for(ChainsBuildComp next : other.chainBuilds()){
        if(added.add(next) && filter.get(next)) findQueue.addFirst(next);
      }
      ChainsContainer oldContainer = other.chains().container;
      add(other);

      other.chainsFlowed(oldContainer);
    }
  }
  
  public void remove(ChainsBuildComp target){
    Seq<ChainsBuildComp> children;
    for(ChainsBuildComp next: children = target.chainBuilds()){
      if(!all.contains(next) || next.chains().container != this) continue;

      next.chains().newContainer().reconstruct(next, e -> e != target && all.contains(e));
    }
    target.chainsRemoved(children);
  }
}
