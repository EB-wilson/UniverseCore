package universecore.world.consumers;

import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.type.PayloadSeq;
import mindustry.type.PayloadStack;
import mindustry.ui.ItemImage;
import mindustry.ui.ReqImage;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ConsumerBuildComp;

public class ConsumePayload<T extends Building & ConsumerBuildComp> extends BaseConsume<T>{
  private static final ObjectMap<UnlockableContent, PayloadStack> TMP = new ObjectMap<>();

  public PayloadStack[] payloads;

  public ConsumePayload(PayloadStack[] payloads){
    this.payloads = payloads;
  }

  @Override
  public ConsumeType<?> type(){
    return ConsumeType.payload;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void merge(BaseConsume<T> other){
    if(other instanceof ConsumePayload cons){
      TMP.clear();
      for(PayloadStack stack: payloads){
        TMP.put(stack.item, stack);
      }

      for(PayloadStack stack: ((ConsumePayload<T>)cons).payloads){
        TMP.get(stack.item, () -> new PayloadStack(stack.item, 0)).amount += stack.amount;
      }

      payloads = TMP.values().toSeq().sort((a, b) -> a.item.id - b.item.id).toArray(PayloadStack.class);
    }
    else throw new IllegalArgumentException("only merge consume with same type");
  }

  @Override
  public float efficiency(T build){
    for(PayloadStack stack : payloads){
      if(!build.getPayloads().contains(stack.item, stack.amount)){
        return 0f;
      }
    }
    return 1f;
  }

  @Override
  public void consume(T build){
    for(PayloadStack stack : payloads){
      build.getPayloads().remove(stack.item, stack.amount);
    }
  }

  @Override
  public void update(T entity){}

  @Override
  public void display(Stats stats){
    for(PayloadStack stack : payloads){
      stats.add(Stat.input, t -> {
        t.add(new ItemImage(stack));
        t.add(stack.item.localizedName).padLeft(4).padRight(4);
      });
    }
  }

  @Override
  public void build(Building build, Table table){
    PayloadSeq inv = build.getPayloads();

    table.table(c -> {
      int i = 0;
      for(var stack : payloads){
        c.add(new ReqImage(new ItemImage(stack.item.uiIcon, stack.amount),
            () -> inv.contains(stack.item, stack.amount))).padRight(8);
        if(++i % 4 == 0) c.row();
      }
    }).left();
  }

  @Override
  public Seq<Content> filter(){
    return Seq.with(payloads).map(s -> s.item);
  }
}
