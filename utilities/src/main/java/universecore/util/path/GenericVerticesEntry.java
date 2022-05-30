package universecore.util.path;

public class GenericVerticesEntry<Vert extends PathVertices<Vert>> implements VerticesEntry<Vert>{
  private final Vert next;
  private final float weight;

  public GenericVerticesEntry(Vert next, float weight){
    this.next = next;
    this.weight = weight;
  }

  @Override
  public Vert nextVert(){
    return next;
  }

  @Override
  public float weight(){
    return weight;
  }
}
