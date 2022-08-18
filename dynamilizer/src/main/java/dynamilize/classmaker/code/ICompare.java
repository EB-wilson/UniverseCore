package dynamilize.classmaker.code;

import dynamilize.classmaker.ElementVisitor;

import java.util.HashMap;
import java.util.Map;

public interface ICompare<T> extends Element{
  @Override
  default void accept(ElementVisitor visitor){
    visitor.visitCompare(this);
  }

  @Override
  default ElementKind kind(){
    return ElementKind.COMPARE;
  }

  ILocal<T> leftNumber();

  ILocal<T> rightNumber();

  Label ifJump();

  Comparison comparison();

  enum Comparison{
    EQUAL("=="),
    UNEQUAL("!="),
    MORE(">"),
    LESS("<"),
    MOREOREQUAL(">="),
    LESSOREQUAL("<=");

    private static final Map<String, Comparison> symbolMap = new HashMap<>();

    static{
      for(Comparison opc: values()){
        symbolMap.put(opc.symbol, opc);
      }
    }

    private final String symbol;

    Comparison(String sym){
      this.symbol = sym;
    }

    public static Comparison as(String symbol){
      return symbolMap.computeIfAbsent(symbol, e -> {throw new IllegalArgumentException("unknown operator symbol: " + e);});
    }
  }
}
