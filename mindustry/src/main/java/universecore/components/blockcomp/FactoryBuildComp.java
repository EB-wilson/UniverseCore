package universecore.components.blockcomp;

import arc.math.Mathf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import universecore.annotations.Annotations;

/**工厂建筑的接口组件，该组件赋予方块执行生产/制造的行为，工厂行为整合了{@link ConsumerBuildComp}和{@link ProducerBlockComp}的行为并描述了制造行为的默认实现
 *
 * @since 1.4
 * @author EBwilson*/
public interface FactoryBuildComp extends ProducerBuildComp{
  /**{@code getter-}生产进度*/
  @Annotations.BindField("progress")
  default float progress(){
    return 0;
  }

  /**{@code setter-}生产进度*/
  @Annotations.BindField("progress")
  default void progress(float value){}

  /**{@code getter-}整体总的生产进度*/
  @Annotations.BindField("totalProgress")
  default float totalProgress(){
    return 0;
  }

  /**{@code setter-}整体总的生产进度*/
  @Annotations.BindField("totalProgress")
  default void totalProgress(float value){}

  /**{@code getter-}工作预热的插值*/
  @Annotations.BindField("warmup")
  default float warmup(){
    return 0;
  }

  /**{@code setter-}工作预热的插值*/
  @Annotations.BindField("warmup")
  default void warmup(float value){}

  @Override
  default float consEfficiency(){
    return ProducerBuildComp.super.consEfficiency()*warmup();
  }

  @Annotations.MethodEntry(entryMethod = "update")
  default void updateFactory(){
    /*当未选择配方时不进行更新*/
    if(produceCurrent() == -1 || producer().current == null){
      warmup(Mathf.lerpDelta(warmup(), 0, getFactoryBlock().stopSpeed()));
      return;
    }

    if(shouldConsume() && consumeValid()){
      progress(progress() + progressIncrease(consumer().current.craftTime));
      warmup(Mathf.lerpDelta(warmup(), 1, getFactoryBlock().warmupSpeed()));

      onCraftingUpdate();
    }
    else{
      warmup(Mathf.lerpDelta(warmup(), 0, getFactoryBlock().stopSpeed()));
    }

    totalProgress(totalProgress() + consumer().consDelta());

    while(progress() >= 1){
      progress(progress() - 1);
      consumer().trigger();
      producer().trigger();

      craftTrigger();
    }
  }

  @Annotations.MethodEntry(entryMethod = "read", paramTypes = {"arc.util.io.Reads -> read", "byte"})
  default void readFactory(Reads read){
    progress(read.f());
    totalProgress(read.f());
    warmup(read.f());
  }

  @Annotations.MethodEntry(entryMethod = "write", paramTypes = "arc.util.io.Writes -> write")
  default void writeFactory(Writes write){
    write.f(progress());
    write.f(totalProgress());
    write.f(warmup());
  }

  /**当前机器的工作效率，0-1*/
  default float workEfficiency(){
    return consEfficiency();
  }

  /**机器工作的销量增量，标准情况下是生产时间的倒数，乘以额外的增量返回
   *
   * @param baseTime 当前执行的消耗的基准耗时*/
  default float progressIncrease(float baseTime){
    return 1/baseTime*consumer().consDelta();
  }

  default FactoryBlockComp getFactoryBlock(){
    return getBlock(FactoryBlockComp.class);
  }

  @Override
  default boolean shouldConsume(){
    return ProducerBuildComp.super.shouldConsume() && productValid();
  }

  /**机器工作中一次生产执行时调用*/
  void craftTrigger();

  /**机器工作中随每一次刷新调用*/
  void onCraftingUpdate();
}
