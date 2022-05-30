package universecore.util.path;

public interface VerticesEntry<Vert extends PathVertices<Vert>>{
  Vert nextVert();

  float weight();
}
