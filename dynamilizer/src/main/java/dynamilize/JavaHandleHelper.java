package dynamilize;

import java.lang.reflect.AccessibleObject;

/**出于jdk16+进一步升级的模块化管理以及跨平台和跨版本考虑，提供java基本行为的平台支持器，这包含了一些与版本/平台有关的行为接口。
 * <p>实施时应当按方法的功能说明针对运行平台进行实现。
 *
 * @author EBwilson */
public interface JavaHandleHelper{
  /**设置访问检查对象的可访问状态为true，jdk16以后{@link AccessibleObject#setAccessible(boolean)}具有模块open检查，
   * 此方法需要额外进行反模块化实现后再设置访问许可。*/
  void setAccess(AccessibleObject object);
}
