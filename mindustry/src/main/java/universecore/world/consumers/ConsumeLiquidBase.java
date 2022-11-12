package universecore.world.consumers;

import universecore.components.blockcomp.ConsumerBuildComp;
import universecore.util.UncLiquidStack;

public abstract class ConsumeLiquidBase<T extends ConsumerBuildComp> extends BaseConsume<T>{
  public UncLiquidStack[] consLiquids;

  @Override
  public ConsumeType<?> type(){
    return ConsumeType.liquid;
  }
}
