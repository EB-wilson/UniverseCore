package universeCore.util.classMakers;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

public class UncField<T> extends Component{
  public final Class<T> type;
  public final String init;
  
  public UncField(Class<T> type, String name){
    this(type, name, null);
  }
  
  public UncField(Class<T> type, String name, String init){
    super(name);
    this.type = type;
    this.init = init;
  }
  
  @Override
  public void handle(CtClass making, UncClass clazz){
    try{
      CtField ctField = new CtField(classPool.get(type.getName()), name, making);
      ctField.setModifiers(getModifiers());
      making.addField(ctField, init);
    }
    catch(NotFoundException | CannotCompileException e){
      e.printStackTrace();
    }
  }
}
