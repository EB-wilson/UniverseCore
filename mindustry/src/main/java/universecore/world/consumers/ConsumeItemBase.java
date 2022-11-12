package universecore.world.consumers;

import mindustry.type.ItemStack;
import universecore.components.blockcomp.ConsumerBuildComp;

public abstract class ConsumeItemBase<T extends ConsumerBuildComp> extends BaseConsume<T>{
  public ItemStack[] consItems;

  @Override
  public ConsumeType<?> type(){
    return ConsumeType.item;
  }
}
