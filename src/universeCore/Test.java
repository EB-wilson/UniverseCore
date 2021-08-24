package universeCore;

import universeCore.util.ini.IniTypes;

public class Test{
  public static void main(String[] args){
    IniTypes.IniBoolean bool = new IniTypes.IniBoolean("true");
    String bo = "true";
    System.out.println(Boolean.valueOf(bo));
  }
}
