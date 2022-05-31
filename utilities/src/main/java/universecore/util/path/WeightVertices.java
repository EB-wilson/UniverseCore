package universecore.util.path;

/**加权有向图的顶点接口，为顶点与顶点之间的边增加权重属性，用于加权有向图的路径搜索。此类顶点不应该被用于无权值的图（尽管它可以正常工作）*/
public interface WeightVertices<Vert extends WeightVertices<Vert> & PathVertices<Vert>> extends PathVertices<Vert>{
  /**获取此顶点到下一个顶点的边的权值，通常由实现者进行权值映射
   *
   * @param next 链接到的下一个顶点
   * @return 这个顶点到传入的下一个顶点间的边权值*/
  float getWeight(Vert next);
}
