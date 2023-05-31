import dynamilize.DynamicMaker;
import dynamilize.ProxyMaker;

import java.util.ArrayList;

public class ProxySample{
  private static final DynamicMaker maker = DynamicMaker.getDefault();
  private static final ProxyMaker proxyMaker = ProxyMaker.getDefault(maker, (proxy, func, args) -> {
    System.out.println("invoke: " + func + ", params: " + args);
    return func.invoke(proxy, args);
  });

  public static void main(String[] args){
    ArrayList<String> list = proxyMaker.newProxyInstance(ArrayList.class).objSelf();
    list.add("first element");
  }
}
