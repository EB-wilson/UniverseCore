package universecore.world.producers;

import arc.Events;
import arc.func.Boolf2;
import arc.func.Func2;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Scaling;
import mindustry.core.UI;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.ui.ItemImage;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import universecore.components.blockcomp.ProducerBuildComp;
import universecore.world.consumers.ConsumePayload;

public class ProducePayload<T extends Building & ProducerBuildComp> extends BaseProduce<T>{
  private static final ObjectMap<UnlockableContent, PayloadStack> TMP = new ObjectMap<>();

  public int displayLim = 4;
  public PayloadStack[] payloads;
  public Func2<T, UnlockableContent, Payload> payloadMaker = this::makePayloadDef;
  public Boolf2<T, UnlockableContent> valid;

  public ProducePayload(PayloadStack[] payloads, Boolf2<T, UnlockableContent> valid){
    this.payloads = payloads;
    this.valid = valid;
  }

  private Payload makePayloadDef(T ent, UnlockableContent type){
    if(type instanceof UnitType unitType){
      Unit unit = unitType.create(ent.team);
      Events.fire(new EventType.UnitCreateEvent(unit, ent));
      return new UnitPayload(unit);
    }
    else if(type instanceof Block block){
      return new BuildPayload(block, ent.team);
    }
    throw new IllegalArgumentException("default payload maker can only make 'Building' and 'Unit', if you want to make other things, please use custom payload maker to field 'payloadMaker'");
  }

  @Override
  public ProduceType<?> type(){
    return ProduceType.payload;
  }

  @Override
  public void buildIcons(Table table) {
    ConsumePayload.buildPayloadIcons(table, payloads, displayLim);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void merge(BaseProduce<T> other){
    if(other instanceof ProducePayload prod){
      TMP.clear();
      for(PayloadStack stack: payloads){
        TMP.put(stack.item, stack);
      }

      for(PayloadStack stack: ((ProducePayload<T>)prod).payloads){
        TMP.get(stack.item, () -> new PayloadStack(stack.item, 0)).amount += stack.amount;
      }

      payloads = TMP.values().toSeq().sort((a, b) -> a.item.id - b.item.id).toArray(PayloadStack.class);
    }
    else throw new IllegalArgumentException("only merge consume with same type");
  }

  @Override
  public void produce(T entity){
    for(PayloadStack stack: payloads){
      for(int i = 0; i < stack.amount; i++){
        Payload payload = payloadMaker.get(entity, stack.item);
        payload.set(entity.x, entity.y, entity.rotdeg());
        if(entity.acceptPayload(entity, payload)) entity.handlePayload(entity, payload);
      }
    }
  }

  @Override
  public boolean valid(T entity){
    for(PayloadStack stack: payloads){
      if(!valid.get(entity, stack.item)) return false;
    }
    return true;
  }

  @Override
  public void update(T entity){}

  @Override
  public void display(Stats stats){
    for(PayloadStack stack : payloads){
      stats.add(Stat.output, t -> {
        t.add(new ItemImage(stack));
        t.add(stack.item.localizedName).padLeft(4).padRight(4);
      });
    }
  }
}
