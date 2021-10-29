package universeCore.world.meta;

import mindustry.world.meta.StatCat;
import universeCore.util.handler.EnumHandler;

public class UncStatCat{
  private static final EnumHandler<StatCat> handler = new EnumHandler<>(StatCat.class);
  
  public static final StatCat other = handler.addEnumItemTail("other");
}
