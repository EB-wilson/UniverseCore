package universecore.world.meta;

import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;
import universecore.util.handler.EnumHandler;

public class UncStat{
  protected static final EnumHandler<Stat> handler = new EnumHandler<>(Stat.class);
  
  public static final Stat optionalInputs  = handler.addEnumItemTail("optionalInputs", UncStatCat.other),
      inputs = handler.addEnumItemTail("inputs", StatCat.crafting);
}
