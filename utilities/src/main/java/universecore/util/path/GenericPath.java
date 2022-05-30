package universecore.util.path;

import java.util.Iterator;
import java.util.LinkedList;

public class GenericPath<Vert extends PathVertices<Vert>> implements Path<Vert>{
  private final LinkedList<Vert> path = new LinkedList<>();

  @Override
  public void addFirst(Vert next){
    path.addFirst(next);
  }

  @Override
  public void addLast(Vert next){
    path.addLast(next);
  }

  @Override
  public Vert origin(){
    return path.getFirst();
  }

  @Override
  public Vert destination(){
    return path.getLast();
  }

  @Override
  public Iterator<Vert> iterator(){
    return path.iterator();
  }
}
