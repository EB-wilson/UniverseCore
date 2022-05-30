package universecore.util.path;

@FunctionalInterface
public interface PathFindFunc<Vert extends PathVertices<Vert>>{
  void accept(Vert destination, Path<Vert> path);
}
