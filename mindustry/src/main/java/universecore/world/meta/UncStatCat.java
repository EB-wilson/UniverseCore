package universecore.world.meta;

import arc.struct.Seq;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;
import universecore.util.handler.FieldHandler;

public class UncStatCat{
  public static final StatCat other = create("other");

  private static StatCat create(String name){
    return create(name, Stat.all.size);
  }

  private static StatCat create(String name, int index){
    Seq<StatCat> all = StatCat.all;
    StatCat res = new StatCat(name);

    FieldHandler.setValueDefault(res, "id", index);
    all.insert(index, res);

    return res;
  }
}
