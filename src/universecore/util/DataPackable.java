package universecore.util;

import arc.func.Func;
import arc.struct.LongMap;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.serialization.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

@SuppressWarnings({"rawtypes", "unchecked"})
public interface DataPackable{
  LongMap<Func> objectProvMap = new LongMap<>();
  
  static void assignType(long typeID, Func<Object[], Object> prov){
    if(objectProvMap.put(typeID, prov) != null)
      throw new SerializationException("conflicting id, type id: " + typeID + "was assigned");
  }
  
  static <T extends DataPackable> T readObject(byte[] bytes, Object... param){
    long id = new Reads(new DataInputStream(new ByteArrayInputStream(bytes))).l();
    Func<Object[], T> objProv = (Func<Object[], T>)objectProvMap.get(id);
    if(objProv == null)
      throw new SerializationException("type id: " + id + " was not assigned");
    
    T result = objProv.get(param);
    result.read(bytes);
    return result;
  }
  
  long typeID();
  
  void write(Writes write);
  
  void read(Reads read);
  
  default byte[] pack(){
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Writes write = new Writes(new DataOutputStream(outputStream));
    write.l(typeID());
    write(write);
    
    return outputStream.toByteArray();
  }
  
  default void read(byte[] bytes){
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
    Reads read = new Reads(new DataInputStream(inputStream));
    if(read.l() != typeID())
      throw new SerializationException("type id was unequal marked type id");
    read(read);
  }
}
