package universecore.util;

import mindustry.content.Liquids;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;

/**提供更多操作的流体堆
 *
 * @since 1.0
 * @author EBwilson
 * @deprecated 无用类型，将废弃*/
@Deprecated
public class UncLiquidStack extends LiquidStack{
  public static final UncLiquidStack[] empty = {};

  public UncLiquidStack(Liquid liquid, float amount){
    if(liquid == null) liquid = Liquids.water;
    this.liquid = liquid;
    this.amount = amount;
  }

  public UncLiquidStack set(Liquid liquid, float amount){
    this.liquid = liquid;
    this.amount = amount;
    return this;
  }

  public UncLiquidStack copy(){
    return new UncLiquidStack(liquid, amount);
  }

  public boolean equals(UncLiquidStack other){
    return other != null && other.liquid == liquid && other.amount == amount;
  }

  public static UncLiquidStack[] mult(UncLiquidStack[] stacks, float multiple){
    UncLiquidStack[] copy = new UncLiquidStack[stacks.length];
    for(int i = 0; i < copy.length; i++){
      copy[i] = new UncLiquidStack(stacks[i].liquid, stacks[i].amount * multiple);
    }
    return copy;
  }
  
  public static UncLiquidStack[] with(Object... liquids){
    UncLiquidStack[] stacks = new UncLiquidStack[liquids.length / 2];
    for(int i = 0; i < liquids.length; i += 2){
      stacks[i / 2] = new UncLiquidStack((Liquid)liquids[i], ((Number)liquids[i + 1]).floatValue());
    }
    return stacks;
  }
  
  @Override
  public String toString(){
    return "LiquidStack{" +
    "liquid=" + liquid +
    ", amount=" + amount +
    '}';
  }
}
