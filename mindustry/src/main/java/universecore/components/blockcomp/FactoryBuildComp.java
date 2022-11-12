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
  @Annotations.BindField("progress")
  default float progress(){
    return 0;
  }

  @Annotations.BindField("progress")
  default void progress(float value){}

  @Annotations.BindField("totalProgress")
  default float totalProgress(){
    return 0;
  }

  @Annotations.BindField("totalProgress")
  default void totalProgress(float value){}

  @Annotations.BindField("warmup")
  default float warmup(){
    return 0;
  }

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

  default float workEfficiency(){
    return consEfficiency();
  }

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

  void craftTrigger();

  void onCraftingUpdate();
}
