package universecore.util.path;

/**基于广度优先搜索的初步寻路实现，实现此接口需要提供必要的容器入口。
 * <p>此搜索<strong>无权值</strong>，产生的路径应为平权最短路径，通常情况下在没有权值的图中查找到的路径一定是最优解或最优之一。
 *
 * @author EBwilson
 * @since 1.3*/
public interface BFSPathFinder<Vert extends PathVertices<Vert>> extends PathFinder<Vert>{
  /**重置搜索状态,包括复位已经遍历过的顶点和边等，你不应该在这个方法实现中清空缓存的路径，这个方法会因为每一个起点搜索路径或者其他情况被多次调用*/
  void reset();

  /**判断一个顶点是否已被遍历过，若尚未遍历，此方法应当返回true并将顶点标记为已遍历；若已遍历，则返回false不进行任何操作
   *
   * @param vert 进行遍历检查的顶点
   * @return 若顶点未遍历则返回true,否则返回false*/
  boolean flowed(Vert vert);

  /**从搜索队列中读取下一个顶点，并将其从队列中弹出，若队列为空则返回null。
   * <p>此方法通与{@link BFSPathFinder#queueAdd(PathVertices)}应当满足堆栈实现，<strong>更晚加入的顶点应当更优先被取出</strong>。
   *
   * @return 一个处于堆栈顶端的顶点，若没有顶点，则返回null*/
  Vert queueNext();

  /**向搜索队列添加一个顶点。
   * <p>此方法通与{@link BFSPathFinder#queueNext()}应当满足堆栈实现，<strong>更晚加入的顶点应当更优先被取出</strong>。
   *
   * @param next 添加到队列中的下一个顶点*/
  void queueAdd(Vert next);

  /**创建一个路径对象，实现时应当能够返回一条空白的路径，你可能还需要对{@link IPath}接口进行实现并返回它的实例，或者直接使用默认实现{@link GenericPath}
   *
   * @return 一条空路径
   * @see IPath
   * @see GenericPath*/
  IPath<Vert> createPath();

  /**创建一个回溯指针对象，默认实现直接创建实例，如果搜索量很大，请将此方法重写使用池存放回溯指针以复用对象，避免创建过多对象造成不必要的回收开销
   *
   * @param self 此指针保存的顶点
   * @param previous 此指针所回溯的前一个指针
   * @return 一个保存了参数信息的指针对象*/
  default PathPointer<Vert> createPointer(Vert self, PathPointer<Vert> previous){
    return new PathPointer<>(self, previous);
  }

  /**一个标准的BFS寻路实现，通常搜寻到的路径是在无权值的图中最短的或最短之一
   *
   * @see PathFinder#findPath(PathVertices, PathFindFunc.PathAcceptor) */
  @Override
  default void findPath(Vert origin, PathFindFunc.PathAcceptor<Vert> pathConsumer){
    reset();
    queueAdd(origin);
    flowed(origin);

    PathPointer<Vert> pointer = createPointer(origin, null);

    Vert next;
    while((next = queueNext()) != null){
      for(Vert entry: next.getLinkVertices()){
        if(flowed(entry)){
          queueAdd(next);
        }
      }

      pointer = createPointer(next, pointer);
      if(isDestination(origin, next)){
        PathPointer<Vert> tracePointer = pointer;
        IPath<Vert> path = createPath();
        path.addFirst(pointer.self);

        while((tracePointer = tracePointer.previous) != null){
          path.addFirst(tracePointer.self);
        }

        pathConsumer.accept(next, path);
      }
    }
  }

  /**基于BFS的图遍历实现，遍历顺序为从给出的起点向外扩散直到每一个顶点都被遍历过
   *
   * @see PathFinder#eachVertices(PathVertices, PathFindFunc.VerticesAcceptor) */
  @Override
  default void eachVertices(Vert origin, PathFindFunc.VerticesAcceptor<Vert> vertConsumer){
    reset();
    queueAdd(origin);
    flowed(origin);

    Vert v;
    while((v = queueNext()) != null){
      for(Vert vert: v.getLinkVertices()){
        if(flowed(vert)){
          queueAdd(vert);
        }
      }
      vertConsumer.accept(v);
    }
  }

  /**路径的回溯指针，每一个指针都保存了一个顶点和它的前一个指针的信息，以类似链表的形式进行迭代回溯，若前一个指针不存在，则说明已到达起点*/
  class PathPointer<Vert>{
    public PathPointer<Vert> previous;

    public Vert self;

    /**构造一个携带给出参数信息的指针对象*/
    public PathPointer(Vert self, PathPointer<Vert> previous){
      this.previous = previous;
      this.self = self;
    }
  }
}
