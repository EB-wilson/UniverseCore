package universeCore.util;

import arc.util.io.Reads;
import arc.util.io.Writes;

import java.io.*;

public interface DataPackable{
  void write(Writes write);
  
  void read(Reads read);
  
  default byte[] pack(){
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    write(new Writes(new DataOutputStream(outputStream)));
    
    return outputStream.toByteArray();
  }
  
  default void read(byte[] bytes){
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
    read(new Reads(new DataInputStream(inputStream)));
  }
}
