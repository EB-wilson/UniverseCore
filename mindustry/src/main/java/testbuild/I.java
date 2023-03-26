package testbuild;

import universecore.annotations.Annotations;

import java.util.Locale;

public interface I {
  @Annotations.BindField("value")
  default void set(int i) {
  }

  @Annotations.BindField("value")
  default int get() {
    return 0;
  }

  @Annotations.MethodEntry(entryMethod = "run", paramTypes = "java.lang.String -> arg")
  default void subTrigger(String arg) {
    System.out.println(arg);
  }

  @Annotations.MethodEntry(entryMethod = "doing", override = true)
  default int doing() {
    System.out.println("trigger");

    return 1;
  }

  @Annotations.MethodEntry(entryMethod = "transToUp", paramTypes = "java.lang.String -> arg", override = true)
  default String transToUp(String arg) {
    return arg.toUpperCase(Locale.ROOT);
  }

  @Annotations.MethodEntry(entryMethod = "transToLow", paramTypes = "java.lang.String -> arg", override = true)
  default String transToLow(String arg) {
    return arg.toLowerCase(Locale.ROOT);
  }
}
