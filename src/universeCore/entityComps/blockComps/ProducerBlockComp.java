package universeCore.entityComps.blockComps;

import universeCore.world.producers.BaseProducers;

import java.util.ArrayList;

/**生产者方块的组件，令方块具有记录输出资源配方的功能
 * 必须创建的变量：
 * <pre>{@code
 *   Seq<Producers> [producers]
 * }<pre/>
 * 若使用非默认命名则需要重写调用方法*/
public interface ProducerBlockComp extends FieldGetter{
  @SuppressWarnings("unchecked")
  default ArrayList<BaseProducers> producers(){
    return (ArrayList<BaseProducers>)getField(ArrayList.class, "producers");
  }
  
  default BaseProducers newProduce(){
    BaseProducers produce = new BaseProducers();
    producers().add(produce);
    return produce;
  }
}
