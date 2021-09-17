package universeCore.util;

/**提供一些复合、高等数学函数的静态方法
 * 全部需要输入一个x坐标，返回其y轴坐标映射*/
public class Functions{
  /***/
  public static double lerp(double origin, double dest, double rate, double x){
    double a = 1 - rate;
    double b = rate*dest;
    
    double powered = Math.pow(a, x - 1);
    
    return origin*powered + (b*powered - b/a)/(1 - 1/a);
  }
}
