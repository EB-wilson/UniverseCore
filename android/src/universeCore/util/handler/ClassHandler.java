package universeCore.util.handler;

import universeCore.util.classMakers.IClassHandler;
import universeCore.util.classMakers.UncClass;
import universeCore.util.classMakers.UncField;
import universeCore.util.classMakers.UncMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

public class ClassHandler implements IClassHandler{
  @Override
  public Class<?> toClass(UncClass<?> clazz){
    return null;
  }
  
  @Override
  public void handleMethod(UncMethod<?> method){
  
  }
  
  @Override
  public void handleField(UncField<?> field){
  
  }
}
