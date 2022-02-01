package universeCore.entityComps.blockComps;

import arc.func.Boolf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.Building;

public interface Dumpable extends BuildCompBase{
  ObjectMap<String, Dumps<?>> dumps();
  
  default void addDumps(String name){
    dumps().put(name, new Dumps<>());
  }
  
  default void addDumps(String name, Seq<?> targets){
    dumps().put(name, new Dumps<>(targets));
  }
  
  default <T> void addDumps(String name, Seq<T> targets, Boolf<T> valid){
    dumps().put(name, new Dumps<>(targets, valid));
  }
  
  @SuppressWarnings("unchecked")
  default <T> Dumps<T> getDumps(String name){
    return (Dumps<T>)dumps().get(name);
  }
  
  default Building getNext(String name){
    return getNext(name, true);
  }
  
  default <T> T getNext(String name, Seq<T> targets){
    return getNext(name, targets, true);
  }
  
  default Building getNext(String name, Boolf<Building> valid){
    return getNext(name, valid, true);
  }
  
  default <T> T getNext(String name, Seq<T> targets, Boolf<T> valid){
    return getNext(name, targets, valid, true);
  }
  
  @SuppressWarnings("unchecked")
  default Building getNext(String name, boolean increase){
    Dumps<Building> dumps;
    if((dumps = (Dumps<Building>)dumps().get(name)) == null){
      dumps = new Dumps<>(getBuilding().proximity);
      dumps().put(name, dumps);
    }
    return increase? dumps.next(): dumps.nextOnly();
  }
  
  @SuppressWarnings("unchecked")
  default Building getNext(String name, Boolf<Building> valid, boolean increase){
    Dumps<Building> dumps;
    if((dumps = (Dumps<Building>)dumps().get(name)) == null){
      dumps = new Dumps<>(getBuilding().proximity, valid);
      dumps().put(name, dumps);
    }
    return increase? dumps.next(valid): dumps.nextOnly(valid);
  }
  
  @SuppressWarnings("unchecked")
  default <T> T getNext(String name, Seq<T> targets, boolean increase){
    Dumps<T> dumps;
    if((dumps = (Dumps<T>)dumps().get(name)) == null){
      dumps = new Dumps<>(targets);
      dumps().put(name, dumps);
    }
    return increase? dumps.next(targets): dumps.nextOnly(targets);
  }
  
  @SuppressWarnings("unchecked")
  default <T> T getNext(String name, Seq<T> targets, Boolf<T> valid, boolean increase){
    Dumps<T> dumps;
    if((dumps = (Dumps<T>)dumps().get(name)) == null){
      dumps = new Dumps<>(targets, valid);
      dumps().put(name, dumps);
    }
    return increase? dumps.next(targets, valid): dumps.nextOnly(targets, valid);
  }
  
  class Dumps<Type>{
    public Seq<Type> targets = new Seq<>();
    public Boolf<Type> valid = e -> true;
    public int countDumps;
    
    public Dumps(){}
    
    public Dumps(Seq<Type> defaultAll){
      this.targets = defaultAll;
    }
  
    public Dumps(Seq<Type> targets, Boolf<Type> valid){
      this.targets = targets;
      this.valid = valid;
    }
  
    public int increaseDumps(int size){
      countDumps = (countDumps + 1)%size;
      return countDumps;
    }
    
    public void setTargets(Seq<Type> other){
      targets = other;
    }
    
    public void setValid(Boolf<Type> other){
      valid = other;
    }
    
    public Type next(){
      return next(targets, valid);
    }
    
    public Type nextOnly(){
      return nextOnly(targets, valid);
    }
    
    public Type next(Boolf<Type> valid){
      return next(targets, valid);
    }
    
    public Type nextOnly(Boolf<Type> valid){
      return nextOnly(targets, valid);
    }
    
    public Type next(Seq<Type> targets){
      return next(targets, valid);
    }
    
    public Type nextOnly(Seq<Type> targets){
      return nextOnly(targets, valid);
    }
    
    public Type next(Seq<Type> targets, Boolf<Type> valid){
      int size = targets.size;
      if(size == 0) return null;
      Type result;
      for(Type ignored : targets){
        result = targets.get(increaseDumps(size));
        if(valid.get(result)) return result;
      }
      return null;
    }
    
    public Type nextOnly(Seq<Type> targets, Boolf<Type> valid){
      int size = targets.size, curr = countDumps;
      if(size == 0) return null;
      Type result;
      for(Type ignored : targets){
        curr = (curr + 1)%size;
        result = targets.get(curr);
        if(valid.get(result)) return result;
      }
      return null;
    }
  }
}
