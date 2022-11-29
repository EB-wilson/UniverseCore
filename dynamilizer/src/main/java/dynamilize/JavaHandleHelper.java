package dynamilize;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**出于jdk16+进一步升级的模块化管理以及跨平台和跨版本考虑，提供java基本行为的平台支持器，这包含了一些与版本/平台有关的行为接口。
 * <p>实施时应当按方法的功能说明针对运行平台进行实现。
 *
 * @author EBwilson */
public interface JavaHandleHelper {

  <T> T newInstance(Constructor<? extends T> cstr, Object... args);

  <R> R invoke(Method method, Object target, Object... args);

  <T> T get(Field field, Object target);

  void set(Field field, Object target, Object value);
}