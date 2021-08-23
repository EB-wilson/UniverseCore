package universeCore.entityComps.blockComps;

import universeCore.world.producers.BaseProducers;

import java.util.ArrayList;

/**�����߷��������������м�¼�����Դ�䷽�Ĺ���
 * ���봴���ı�����
 * <pre>{@code
 *   Seq<Producers> [producers]
 * }<pre/>
 * ��ʹ�÷�Ĭ����������Ҫ��д���÷���*/
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
