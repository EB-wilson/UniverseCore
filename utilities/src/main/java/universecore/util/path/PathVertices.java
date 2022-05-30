package universecore.util.path;

public interface PathVertices<Vert extends PathVertices<Vert>>{
  Iterable<VerticesEntry<Vert>> getLinkVertices();
}
