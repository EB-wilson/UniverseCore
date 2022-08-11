package universecore.util.handler;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ObjectHandler{
  public static <S, T extends S> void copyField(S source, T target){
    copyFieldAsBlack(source, target);
  }

  public static <S, T extends S> void copyFieldAsBlack(S source, T target, String... blackList){
    Class<?> curr = source.getClass();
    Set<String> black = new HashSet<>(Arrays.asList(blackList));
    Set<String> fields = new HashSet<>();

    while(curr != Object.class){
      for(Field field: curr.getDeclaredFields()){
        fields.add(field.getName());
      }

      curr = curr.getSuperclass();
    }

    copyFieldAsWhite(source, target, fields.stream().filter(e -> !black.contains(e)).toArray(String[]::new));
  }

  public static <S, T extends S> void copyFieldAsWhite(S source, T target, String... whiteList){
    for(String s: whiteList){
      FieldHandler.setValueDefault(target, s, FieldHandler.getValueDefault(source, s));
    }
  }
}
