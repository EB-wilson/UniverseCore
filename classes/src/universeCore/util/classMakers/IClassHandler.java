package universeCore.util.classMakers;

public interface IClassHandler{
  Class<?> toClass(UncClass<?> clazz);
  
  void handleMethod(UncMethod<?> method);
  
  void handleField(UncField<?> field);
}
