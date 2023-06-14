package universecore.world.blocks.modules;

import arc.func.Cons;
import arc.util.io.Writes;
import mindustry.world.modules.BlockModule;
import universecore.components.ExtraVariableComp;
import universecore.components.blockcomp.ChainsBuildComp;
import universecore.world.blocks.chains.ChainsContainer;

import java.util.Map;

public class ChainsModule extends BlockModule implements ExtraVariableComp {
  public ChainsBuildComp entity;
  public ChainsContainer container;
  
  public ChainsModule(ChainsBuildComp entity){
    this.entity = entity;
  }
  
  public ChainsContainer newContainer(){
    ChainsContainer old = entity.chains().container;

    entity.chains().container = new ChainsContainer();
    entity.containerCreated(old);

    entity.chains().container.add(entity);

    return entity.chains().container;
  }
  
  public void each(Cons<ChainsBuildComp> cons){
    for(ChainsBuildComp other: container.all){
      cons.get(other);
    }
  }

  @Override
  public Map<String, Object> extra() {
    return container.extra();
  }

  /**@deprecated 请使用setVar(String, Object)
   * @see ChainsModule#setVar(String, Object) */
  @Deprecated
  public void putVar(String key, Object obj){
    container.setVar(key, obj);
  }
  
  @Override
  public void write(Writes write){}
}
