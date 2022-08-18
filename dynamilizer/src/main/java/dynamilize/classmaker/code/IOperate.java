package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

import java.util.HashMap;
import java.util.Map;

public interface IOperate<T> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitOperate(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.OPERATE;
  }

  OPCode opCode();

  ILocal<?> resultTo();

  ILocal<T> leftOpNumber();

  ILocal<T> rightOpNumber();

  enum OPCode{
    ADD("+"),
    SUBSTRUCTION("-"),
    MULTI("*"),
    DIVISION("/"),
    REMAINING("%"),

    LEFTMOVE("<<"),
    RIGHTMOVE(">>"),
    UNSIGNMOVE(">>>"),
    BITSAME("&"),
    BITOR("|"),
    BITXOR("^");

    private static final Map<String, OPCode> symbolMap = new HashMap<>();

    static{
      for(OPCode opc: values()){
        symbolMap.put(opc.symbol, opc);
      }
    }

    private final String symbol;

    OPCode(String sym){
      this.symbol = sym;
    }

    public static OPCode as(String symbol){
      return symbolMap.computeIfAbsent(symbol, e -> {throw new IllegalArgumentException("unknown operator symbol: " + e);});
    }
  }
}
