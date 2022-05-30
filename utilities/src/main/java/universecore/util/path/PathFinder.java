package universecore.util.path;

public interface PathFinder<Vert extends PathVertices<Vert>>{
  boolean isDestination(Vert vert);

  void findPath(Vert origin, PathFindFunc<Vert> pathConsumer);
}
