package universecore.util.path;

/**基本路径搜索器的基类，子类或接口需要使用一种或几种算法来实现路径搜索方法，此模型内由顶点和其指向的顶点表示为一个有向图
 *
 * @author EBwilson
 * @since 1.3*/
public interface PathFinder<Vert extends PathVertices<Vert>>{
  /**判断从给出的起点，目标顶点是否为这个起点的一个终点，这回决定网络的搜索结果
   *
   * @param origin 搜索开始的起点
   * @param vert 要要判断是否是一个目的地的顶点
   * @return 如果对这个起点而言，vert是一个终点则返回true,否则返回false*/
  boolean isDestination(Vert origin, Vert vert);

  /**从一个起点开始寻找图中的路径，应当能够到达所有的终点，并将搜索结果通过回调函数回调，这需要有算法的具体实现
   * <p>实现上，对于一个起点，其应该对所有可到达终点都能产生路径，并通过回调函数传回路径和终点信息
   *
   * @param origin 搜索的原点，从这个点开始搜索整个图
   * @param pathConsumer 路径搜索的数据回调函数，每产生一条路径，就会将这个路径与其终点作为参数传递给此回调函数*/
  void findPath(Vert origin, PathFindFunc.PathAcceptor<Vert> pathConsumer);

  /**从给出的原始位置开始遍历图中所有的顶点，从回调函数接收每一个顶点
   * <p>实现应当正确的输出所有的顶点，每一个顶点都应当对函数进行一次回调输出
   *
   * @param origin 遍历的起点
   * @param vertConsumer 顶点的回调函数，每一个顶点都从这个函数进行回调接收*/
  void eachVertices(Vert origin, PathFindFunc.VerticesAcceptor<Vert> vertConsumer);
}
