package dynamilize;

import java.util.Arrays;
import java.util.Objects;

class ClassImplements<T>{
  final Class<T> base;
  final Class<?>[] interfaces;
  private int hash;

  public ClassImplements(Class<T> base, Class<?>[] interfaces){
    this.base = base;
    this.interfaces = interfaces;
    this.hash = Objects.hash(base);
    hash = 31*hash + Arrays.hashCode(interfaces);
  }

  @Override
  public String toString(){
    return base.getCanonicalName() + Arrays.hashCode(Arrays.stream(interfaces).map(Class::getCanonicalName).toArray(String[]::new));
  }

  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(!(o instanceof ClassImplements<?> that)) return false;
    return base.equals(that.base) && interfaces.length == that.interfaces.length && hash == ((ClassImplements<?>) o).hash;
  }

  @Override
  public int hashCode(){
    return hash;
  }
}
