package universeCore.internal;

import arc.files.Fi;

import javax.tools.SimpleJavaFileObject;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class JavaStringObject extends SimpleJavaFileObject{
  protected final String sourceCode;
  
  protected ByteArrayOutputStream outputStream;
  
  public JavaStringObject(String name, String sourceCode){
    super(getClassURI(name + ".java"), Kind.SOURCE);
    this.sourceCode = sourceCode;
  }
  
  public JavaStringObject(String name, Kind kind){
    super(getClassURI(name), kind);
    sourceCode = null;
  }
  
  private static URI getClassURI(String className){
    try{
      return new URI(className);
    }catch(URISyntaxException e){
      throw new IllegalArgumentException(e);
    }
  }
  
  @Override
  public InputStream openInputStream(){
    return new ByteArrayInputStream(toByteCode());
  }
  
  @Override
  public OutputStream openOutputStream(){
    return outputStream = new ByteArrayOutputStream();
  }
  
  @Override
  public String getCharContent(boolean ignoreEncodingErrors){
    return sourceCode;
  }
  
  public byte[] toByteCode(){
    return outputStream.toByteArray();
  }
  
  public void writeToFile(Fi file) throws IOException{
    OutputStream writer = file.write(false);
    writer.write(toByteCode());
  }
  
  public void writeToFile(String file) throws IOException{
    FileOutputStream out = new FileOutputStream(file);
    out.write(toByteCode());
  }
}
