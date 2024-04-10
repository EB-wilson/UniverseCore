package universecore.components.blockcomp;


import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import universecore.annotations.Annotations;
import universecore.world.blocks.modules.BaseProductModule;
import universecore.world.producers.BaseProduce;
import universecore.world.producers.ProducePower;
import universecore.world.producers.ProduceType;

/**生产者组件，令方块具有按需进行资源生产输出的能力
 *
 * @author EBwilson
 * @since 1.0*/
public interface ProducerBuildComp extends BuildCompBase, ConsumerBuildComp{
  /**当前选择的生产项目的索引*/
  default int produceCurrent(){
    return consumeCurrent();
  }

  @Annotations.BindField("powerProdEfficiency")
  default float powerProdEfficiency(){
    return 0;
  }

  @Annotations.BindField("powerProdEfficiency")
  default void powerProdEfficiency(float powerProdEfficiency){}

  default float prodMultiplier(){
    return consMultiplier();
  }

  /**生产组件*/
  @Annotations.BindField("producer")
  default BaseProductModule producer(){
    return null;
  }

  @Annotations.MethodEntry(entryMethod = "update")
  default void updateProducer(){
    producer().update();
  }
  
  /**获得该块的NuclearEnergyBlock*/
  default ProducerBlockComp getProducerBlock(){
    return getBlock(ProducerBlockComp.class);
  }
  
  /**获得该块的NuclearEnergyBlock*/
  default ProducerBuildComp getProducerBuilding(){
    return getBlock(ProducerBuildComp.class);
  }

  /**当前生产是否可用*/
  default boolean productValid(){
    return producer() == null || producer().valid();
  }

  /**当前是否应当执行生产项更新*/
  default boolean shouldProduct(){
    return producer() != null && produceCurrent() != -1;
  }

  @SuppressWarnings("unchecked")
  default void buildProducerBars(Table bars){
    if (consumer().current != null){
      for (BaseProduce<? extends ProducerBuildComp> consume : producer().current.all()) {
        ((BaseProduce<ProducerBuildComp>) consume).buildBars(this, bars);
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Annotations.MethodEntry(entryMethod = "getPowerProduction", override = true)
  default float getPowerProduction(){
    if(!getBlock().outputsPower || producer().current == null || producer().current.get(ProduceType.power) == null) return 0;
    powerProdEfficiency(Mathf.num(shouldConsume() && consumeValid())*consEfficiency()*((ProducePower)(producer().current.get(ProduceType.power))).multiple(this));
    return producer().getPowerProduct()*powerProdEfficiency();
  }
}
