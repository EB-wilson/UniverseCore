package universeCore.util.entityProxy;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;
import mindustry.type.Item;
import mindustry.type.Liquid;
import universeCore.util.handler.ProxyHandler.MethodLambda;

public class BaseBuildProxy extends BaseEntityProxy{
  public BaseBuildProxy(Object object){
    super(object);
  }
  
  public MethodLambda created(){
    return defaults;
  }
  
  public MethodLambda consValid(){
    return defaults;
  }
  
  public MethodLambda consume(){
    return defaults;
  }
  
  public MethodLambda delta(){
    return defaults;
  }
  
  public MethodLambda edelta(){
    return defaults;
  }
  
  public MethodLambda efficiency(){
    return defaults;
  }
  
  public MethodLambda status(){
    return defaults;
  }
  
  public MethodLambda shouldConsume(){
    return defaults;
  }
  
  public MethodLambda productionValid(){
    return defaults;
  }
  
  public MethodLambda getPowerProduction(){
    return defaults;
  }
  
  public MethodLambda configTapped(){
    return defaults;
  }
  
  public MethodLambda handleItem(Building var1, Item var2){
    return defaults;
  }
  
  public MethodLambda acceptItem(Building var1, Item var2){
    return defaults;
  }
  
  public MethodLambda acceptLiquid(Building var1, Liquid var2){
    return defaults;
  }
  
  public MethodLambda handleLiquid(Building var1, Liquid var2, float var3){
    return defaults;
  }
  
  public MethodLambda dumpLiquid(Liquid var1){
    return defaults;
  }
  
  public MethodLambda dumpLiquid(Liquid var1, float var2){
    return defaults;
  }
  
  public MethodLambda canDumpLiquid(Building var1, Liquid var2){
    return defaults;
  }
  
  public MethodLambda transferLiquid(Building var1, float var2, Liquid var3){
    return defaults;
  }
  
  public MethodLambda moveLiquidForward(boolean var1, Liquid var2){
    return defaults;
  }
  
  public MethodLambda moveLiquid(Building var1, Liquid var2){
    return defaults;
  }
  
  public MethodLambda updateTile(){
    return defaults;
  }
  
  @Override
  public MethodLambda update(){
    return defaults;
  }
  
  @Override
  public MethodLambda write(Writes var1){
    return defaults;
  }
  
  @Override
  public MethodLambda read(Reads var1){
    return defaults;
  }
  
  @Override
  public Class<?> proxyType(){
    return Buildingc.class;
  }
}
