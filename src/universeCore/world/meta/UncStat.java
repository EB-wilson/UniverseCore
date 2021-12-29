package universeCore.world.meta;

import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;
import universeCore.util.handler.EnumHandler;
import universeCore.util.handler.FieldHandler;

public class UncStat{
  protected static final EnumHandler<Stat> handler = new EnumHandler<>(Stat.class){
    @Override
    public void constructor(Stat instance, Object... param){
      if(param.length == 0){
        FieldHandler.setValue(Stat.class, "category", instance, StatCat.general);
      }
      else{
        FieldHandler.setValue(Stat.class, "category", instance, param[0]);
      }
    }
  };
  
  public static final Stat optionalInputs  = handler.addEnumItemTail("optionalInputs", UncStatCat.other),
      inputs = handler.addEnumItemTail("inputs", StatCat.crafting);
}