package universeCore.internal;

import javax.tools.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class UncJavaFileManager extends ForwardingJavaFileManager<JavaFileManager>{
  protected UncClassLoader uncClassLoader;
  
  protected final HashMap<URI, JavaStringObject> fileObjectMap = new HashMap<>();
  
  protected UncJavaFileManager(JavaFileManager fileManager, UncClassLoader loader){
    super(fileManager);
    uncClassLoader = loader;
  }
  
  private static URI getURILocation(Location location, String packageName, String relativeName){
    try{
      return new URI(location.getName() + '/' + packageName + '/' + relativeName);
    }catch(URISyntaxException e){
      throw new IllegalArgumentException(e);
    }
  }
  
  public void addFile(Location location, String packageName, String relativeName, JavaStringObject file){
    fileObjectMap.put(getURILocation(location, packageName, relativeName), file);
  }
  
  @Override
  public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException{
    JavaStringObject fileObject = fileObjectMap.get(getURILocation(location, packageName, relativeName));
    if(fileObject != null) return fileObject;
    return super.getFileForInput(location, packageName, relativeName);
  }
  
  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException{
    JavaStringObject fileObject = new JavaStringObject(className, kind);
    uncClassLoader.addObject(className, fileObject);
    return fileObject;
  }
  
  @Override
  public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException{
    ArrayList<JavaFileObject> result = new ArrayList<>();
  
    for(JavaFileObject javaFileObject : super.list(location, packageName, kinds, recurse)){
      result.add(javaFileObject);
    }
  
    if(location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)){
      for(JavaFileObject file : fileObjectMap.values()) {
        if(file.getKind() == JavaFileObject.Kind.CLASS && file.getName().startsWith(packageName)){
          result.add(file);
        }
      }
      
      result.addAll(uncClassLoader.fileList());
    }
    else if(location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)){
      for(JavaFileObject file : fileObjectMap.values()){
        if(file.getKind() == JavaFileObject.Kind.SOURCE && file.getName().startsWith(packageName)){
          result.add(file);
        }
      }
    }
    
    return result;
  }
  
  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    if (file instanceof JavaStringObject) {
      return file.getName();
    }
    return super.inferBinaryName(location, file);
  }
  
  @Override
  public ClassLoader getClassLoader(Location location){
    return uncClassLoader;
  }
}
