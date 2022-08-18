package dynamilize;

public interface IVariable{
  String name();

  boolean isConst();

  <T> T get(DynamicObject<?> obj);

  void set(DynamicObject<?> obj, Object value);
}
