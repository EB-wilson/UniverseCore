package universecore.androidcore.classes.dexmaker;

import com.android.dx.dex.file.EncodedField;
import com.android.dx.rop.cst.*;
import com.android.dx.rop.type.Type;
import dynamilize.IllegalHandleException;
import dynamilize.classmaker.code.IField;

import java.lang.reflect.Array;

public class DexFieldInfo{
  private final IField<?> field;
  private final Constant constant;

  public DexFieldInfo(IField<?> field){
    this.field = field;
    this.constant = toConstant(field.initial());
  }

  public String name(){
    return field.name();
  }

  public int modifiers(){
    return field.modifiers();
  }

  public Constant getConstant(){
    return constant;
  }

  public EncodedField toItem(){
    return null;
  }

  private static Constant toConstant(Object initial){
    if(initial instanceof Boolean b) return CstBoolean.make(b);
    else if(initial instanceof Byte b) return CstByte.make(b);
    else if(initial instanceof Short s) return CstShort.make(s);
    else if(initial instanceof Integer i) return CstInteger.make(i);
    else if(initial instanceof Character c) return CstChar.make(c);
    else if(initial instanceof Long l) return CstLong.make(l);
    else if(initial instanceof Float f) return CstFloat.make(Float.floatToIntBits(f));
    else if(initial instanceof Double d) return CstDouble.make(Double.doubleToLongBits(d));
    else if(initial instanceof String s) return new CstString(s);
    else if(initial instanceof Class<?> s) return new CstType(
        Type.intern("L" + s.getName().replace(".", "/")));
    else if(initial instanceof Enum<?> e)
      return new CstEnumRef(
          new CstNat(
              new CstString(e.name()),
              new CstString("L" + e.getClass().getName().replace(".", "/"))
          )
      );
    else if(initial.getClass().isArray()){
      int len = Array.getLength(initial);
      CstArray.List list = new CstArray.List(len);

      for(int i = 0; i < len; i++){
        list.set(i, toConstant(Array.get(initial, i)));
      }

      return new CstArray(list);
    }
    else throw new IllegalHandleException("invalid constant value: " + initial);
  }
}
