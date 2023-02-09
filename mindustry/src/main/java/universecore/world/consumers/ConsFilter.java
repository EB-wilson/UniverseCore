package universecore.world.consumers;

import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import mindustry.ctype.Content;
import universecore.components.blockcomp.ConsumerBuildComp;

public class ConsFilter{
  public ObjectMap<ConsumeType<?>, ObjectSet<Content>> optionalFilter = new ObjectMap<>();
  public ObjectMap<ConsumeType<?>, ObjectSet<Content>> allFilter = new ObjectMap<>();

  public void applyFilter(Iterable<BaseConsumers> consumers, Iterable<BaseConsumers> optional){
    if(optional != null){
      for(BaseConsumers cons: optional){
        handle(cons, optionalFilter);
        handle(cons, allFilter);
      }
    }

    if(consumers != null){
      for(BaseConsumers cons: consumers){
        handle(cons, allFilter);
        for(ObjectMap.Entry<ConsumeType<?>, ObjectSet<Content>> access: cons.selfAccess){
          allFilter.get(access.key, ObjectSet::new).addAll();
        }
      }
    }
  }

  /**过滤器，将判断对当前选中的区域指定type下对输入的对象是否接受
   * 若可选过滤器已添加目标对象同样返回true
   *
   * @param type 过滤器种类
   * @param target 通过过滤器的目标对象
   * @param acceptAll 是否接受所有清单的需求
   * @return 布尔值，是否接受此对象
   * */
  public boolean filter(ConsumerBuildComp entity, ConsumeType<?> type, Content target, boolean acceptAll){
    if(optionalFilter.containsKey(type) && optionalFilter.get(type).contains(target)) return true;

    if(acceptAll) return allFilter.containsKey(type) && allFilter.get(type).contains(target);

    return entity.consumer().current != null && entity.consumer().current.filter(type, target);
  }

  private void handle(BaseConsumers cons, ObjectMap<ConsumeType<?>, ObjectSet<Content>> map){
    for(BaseConsume<?> c: cons.all()){
      if((c.filter()) != null){
        ObjectSet<Content> all = allFilter.get(c.type(), ObjectSet::new);
        ObjectSet<Content> set = map.get(c.type(), ObjectSet::new);
        for(Content o: c.filter()){
          set.add(o);
          all.add(o);
        }
      }
    }
  }
}
