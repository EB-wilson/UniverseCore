package universecore.util;

import arc.func.Cons;
import arc.struct.Seq;
import mindustry.content.TechTree;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Objectives;
import mindustry.type.ItemStack;

public class TechTreeConstructor{
  public final TechTree.TechNode node;

  public static TechTree.TechNode get(UnlockableContent content){
    return TechTree.all.find(e -> e.content == content);
  }

  public static void nodeProduce(UnlockableContent parent, UnlockableContent content, Seq<Objectives.Objective> objectives,
                           Cons<TechTreeConstructor> child){
    new TechTreeConstructor(parent, content, content.researchRequirements(), objectives.add(new Objectives.Produce(content)), child);
  }

  public static void nodeProduce(UnlockableContent parent, UnlockableContent content, Cons<TechTreeConstructor> children){
    nodeProduce(parent, content, new Seq<>(), children);
  }

  public static void nodeProduce(TechTree.TechNode parent, UnlockableContent content, Seq<Objectives.Objective> objectives, Cons<TechTreeConstructor> child){
    new TechTreeConstructor(parent, content, content.researchRequirements(), objectives.add(new Objectives.Produce(content)), child);
  }

  public static void nodeProduce(TechTree.TechNode parent, UnlockableContent content, Cons<TechTreeConstructor> children){
    node(parent, content, new Seq<>(), children);
  }

  public static void node(UnlockableContent parent, UnlockableContent content, Seq<Objectives.Objective> objectives, Cons<TechTreeConstructor> child){
    new TechTreeConstructor(parent, content, content.researchRequirements(), objectives, child);
  }

  public static void node(UnlockableContent parent, UnlockableContent content, Cons<TechTreeConstructor> children){
    node(parent, content, new Seq<>(), children);
  }

  public static void node(TechTree.TechNode parent, UnlockableContent content, Seq<Objectives.Objective> objectives,
                    Cons<TechTreeConstructor> child){
    new TechTreeConstructor(parent, content, content.researchRequirements(), objectives, child);
  }

  public static void node(TechTree.TechNode parent, UnlockableContent content, Cons<TechTreeConstructor> children){
    node(parent, content, new Seq<>(), children);
  }

  public TechTreeConstructor(TechTree.TechNode parent, UnlockableContent research, ItemStack[] requirements, Seq<Objectives.Objective> objectives, Cons<TechTreeConstructor> child){
    node = new TechTree.TechNode(parent, research, requirements);
    if(objectives != null) node.objectives.addAll(objectives);
    child.get(this);
  }

  public TechTreeConstructor(UnlockableContent parent, UnlockableContent research, Cons<TechTreeConstructor> child){
    this(parent, research, research.researchRequirements(), null, child);
  }

  public TechTreeConstructor(UnlockableContent parent, UnlockableContent research, Seq<Objectives.Objective> objectives, Cons<TechTreeConstructor> child){
    this(parent, research, research.researchRequirements(), objectives, child);
  }

  public TechTreeConstructor(UnlockableContent parent, UnlockableContent research, ItemStack[] requirements, Cons<TechTreeConstructor> child){
    this(parent, research, requirements, null, child);
  }

  public TechTreeConstructor(UnlockableContent parent, UnlockableContent research, ItemStack[] requirements, Seq<Objectives.Objective> objectives, Cons<TechTreeConstructor> child){
    this(get(parent), research, requirements, objectives, child);
  }

  public void node(UnlockableContent research, Cons<TechTreeConstructor> child){
    node(research, research.researchRequirements(), null, child);
  }

  public void node(UnlockableContent research, Seq<Objectives.Objective> objectives, Cons<TechTreeConstructor> child){
    node(research, research.researchRequirements(), objectives, child);
  }

  public void node(UnlockableContent research, ItemStack[] requirements, Cons<TechTreeConstructor> child){
    node(research, requirements, null, child);
  }

  public void node(UnlockableContent research, ItemStack[] requirements, Seq<Objectives.Objective> objectives, Cons<TechTreeConstructor> child){
    new TechTreeConstructor(node, research, requirements, objectives, child);
  }

  public void nodeProduce(UnlockableContent content, Cons<TechTreeConstructor> children){
    nodeProduce(content, new Seq<>(), children);
  }

  public void nodeProduce(UnlockableContent content, Seq<Objectives.Objective> objectives, Cons<TechTreeConstructor> child){
    new TechTreeConstructor(node, content, content.researchRequirements(), objectives.add(new Objectives.Produce(content)), child);
  }
}
