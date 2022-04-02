package universecore.util.colletion;

import java.util.*;
import java.util.function.Function;

public class TreeSeq<Type> implements Iterable<Type>{
  private final LinkedList<Type> tmp = new LinkedList<>();

  Comparator<Type> comparator;

  int size;

  TreeSet<LinkedList<Type>> set;

  public TreeSeq(Comparator<Type> cmp){
    comparator = cmp;
    set = new TreeSet<>((a, b) -> cmp.compare(a.getFirst(), b.getFirst()));
  }

  public TreeSeq(){
    set = new TreeSet<>();
  }

  public void add(Type item){
    tmp.clear();
    tmp.addFirst(item);
    LinkedList<Type> t = set.ceiling(tmp);
    if(t == null || set.floor(tmp) != t){
      t = new LinkedList<>();
      t.addFirst(item);
      set.add(t);
    }
    else{
      t.addFirst(item);
    }
    size++;
  }

  public boolean remove(Type item){
    tmp.clear();
    tmp.addFirst(item);LinkedList<Type> t = set.ceiling(tmp);
    if(t != null && set.floor(tmp) == t){
      if(t.size() == 1) set.remove(t);
      t.remove(item);
      size--;
      return true;
    }
    return false;
  }

  public int size(){
    return size;
  }

  public boolean removeIf(Function<Type, Boolean> boolf){
    boolean test = false;
    TreeItr itr = iterator();
    Type item;
    while(itr.hasNext()){
      item = itr.next();
      if(boolf.apply(item)){
        itr.remove();
        size--;
        test = true;
      }
    }

    return test;
  }

  public void clear(){
    set.clear();
  }

  public boolean isEmpty(){
    return set.isEmpty();
  }

  public Type[] toArray(Type[] arr){
    Type[] list = Arrays.copyOf(arr, size);
    int index = 0;
    for(Type item: this){
      list[index++] = item;
    }
    return list;
  }

  @Override
  public TreeItr iterator(){
    return new TreeItr();
  }

  @Override
  public String toString(){
    StringBuilder builder = new StringBuilder("{");
    for(LinkedList<Type> list: set){
      builder.append(list).append(", ");
    }
    return builder.substring(0, builder.length() - 2) + "}";
  }

  public class TreeItr implements Iterator<Type>{
    Iterator<LinkedList<Type>> itr = set.iterator();
    Iterator<Type> listItr;
    LinkedList<Type> curr;

    @Override
    public boolean hasNext(){
      return (listItr != null && listItr.hasNext()) || (itr.hasNext() && (listItr = (curr = itr.next()).iterator()).hasNext());
    }

    @Override
    public Type next(){
      return listItr.next();
    }

    @Override
    public void remove(){
      listItr.remove();
      if(curr.isEmpty()) itr.remove();
    }
  }
}
