package universecore.util.path;

public interface WeightlesslyPathFinder<Vert extends PathVertices<Vert>> extends PathFinder<Vert>{
  void reset();

  boolean flowed(Vert vert);

  Vert queueNext();

  void queueAdd(Vert next);

  PathPointer<Vert> createPointer(Vert self, PathPointer<Vert> last);

  Path<Vert> createPath();

  @Override
  default void findPath(Vert origin, PathFindFunc<Vert> pathConsumer){
    reset();
    queueAdd(origin);

    PathPointer<Vert> pointer = createPointer(origin, null);

    Vert next;
    while((next = queueNext()) != null){
      for(VerticesEntry<Vert> entry: next.getLinkVertices()){
        if(flowed((Vert) entry)){
          queueAdd(next);
        }
      }

      pointer = createPointer(next, pointer);
      if(isDestination(next)){
        PathPointer<Vert> tracePointer = pointer;
        Path<Vert> path = createPath();
        path.addFirst(pointer.self);

        while((tracePointer = tracePointer.previous) != null){
          path.addFirst(tracePointer.self);
        }

        pathConsumer.accept(next, path);
      }
    }
  }

  class PathPointer<Vert>{
    PathPointer<Vert> previous;

    Vert self;
  }
}
