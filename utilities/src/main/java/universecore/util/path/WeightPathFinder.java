package universecore.util.path;

/**有权路径索引的接口，实现此接口时算法必须考虑边的权重，应当使用有权路径搜索算法.
 * <p>关于路径搜索器，请参阅：{@link PathFinder}
 *
 * @author EBwilson
 * @since 1.3
 * @see PathFinder*/
public interface WeightPathFinder<Vert extends PathVertices<Vert> & WeightVertices<Vert>> extends PathFinder<Vert>{

}
