package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

import java.util.HashMap;
import java.util.Map;

public interface IOddOperate<T> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitOddOperate(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.ODDOPERATE;
  }

  ILocal<T> operateNumber();

  ILocal<T> resultTo();

  OddOperator opCode();

  enum OddOperator{
    NEGATIVE("-"),
    BITNOR("~");

    private static final Map<String, OddOperator> symbolMap = new HashMap<>();

    static{
      for(OddOperator opc: values()){
        symbolMap.put(opc.symbol, opc);
      }
    }

    private final String symbol;

    OddOperator(String sym){
      this.symbol = sym;
    }

    public static OddOperator as(String symbol){
      return symbolMap.computeIfAbsent(symbol, e -> {throw new IllegalArgumentException("unknown operator symbol: " + e);});
    }
  }
}
