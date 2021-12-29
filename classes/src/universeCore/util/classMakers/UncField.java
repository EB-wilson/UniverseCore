package universeCore.util.classMakers;

import java.util.List;

public class UncField<T> extends Component{
  public final Class<T> type;
  public final String init;
  
  public UncField(String name, Class<T> type){
    this(name, type, null);
  }
  
  
  public UncField(String name, Class<T> type, String init){
    super(name);
    this.type = type;
    this.init = init;
  }
}
