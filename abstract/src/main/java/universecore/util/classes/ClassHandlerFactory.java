package universecore.util.classes;

import universecore.util.handler.ClassHandler;

public interface ClassHandlerFactory{
  ClassHandler getHandler(Class<?> callerClass);
}
