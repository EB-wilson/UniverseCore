package universecore.util.path;

/**图的路径顶点接口，是构成图的入口，以链接方式与其他顶点构成一个有向图*/
public interface PathVertices<Vert extends PathVertices<Vert>>{
  /**获取此顶点链接的其他顶点的可迭代对象，这应该正确的提供该顶点所链接的其他顶点
   * <pre>{@code
   * 若节点A的此方法可迭代节点B C D，这就相当于:
   * ┌───┐   ┌───┐   ┌───┐
   * │ A ├───► B ├───► E │
   * └┬─┬┘   └───┘   └───┘
   *  │ │
   *  │ └────▲───┐   ┌───┐
   *  │      │ D ├───► F │
   * ┌▼──┐   └─┬─┘   └───┘
   * │ C │     │
   * └───┘     │     ┌───┐
   *           └─────► G │
   *                 └───┘
   * 同样的，其中B可以迭代E，D可以迭代F和G
   * }</pre>*/
  Iterable<Vert> getLinkVertices();
}
