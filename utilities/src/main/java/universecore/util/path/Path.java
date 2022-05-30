package universecore.util.path;

public interface Path<Vert extends PathVertices<Vert>> extends Iterable<Vert>{
  void addFirst(Vert next);

  void addLast(Vert next);

  Vert origin();

  Vert destination();
}
