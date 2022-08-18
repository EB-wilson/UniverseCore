package universecore.desktop9core;

import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import universecore.desktopcore.DesktopFieldAccessHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class DesktopFieldAccessHelper9 extends DesktopFieldAccessHelper{
  private static final ObjectMap<String, Field> EMP_FIEDL = new ObjectMap<>();

  private static final ObjectMap<Class<?>, ObjectMap<String, Field>> fieldPool = new ObjectMap<>();

  private static final boolean useUnsafe;

  static{
    boolean tmp;
    try{
      Class.forName("sun.misc.Unsafe");
      tmp = true;
    }catch(ClassNotFoundException e){
      tmp = false;
    }
    useUnsafe = tmp;
  }

  @Override
  protected void initField(Class<?> clazz, String field){
    if(useUnsafe){
      if(!fieldPool.get(clazz, ObjectMap::new).containsKey(field)) return;

      try{
        Field f = findField(clazz, field);
        fieldPool.get(clazz, ObjectMap::new).put(field, f);
      }catch(NoSuchFieldException e){
        throw new RuntimeException(e);
      }

      return;
    }

    if(!initiled.get(clazz, ObjectSet::new).contains(field)) return;
    try{
      Field f = findField(clazz, field);

      if(Modifier.isFinal(f.getModifiers())){
        finalFields.get(clazz, ObjectMap::new).put(field, f);

        Field mod = Field.class.getDeclaredField("modifiers");
        Demodulator.checkAndMakeModuleOpen(
            Field.class.getModule(),
            Field.class.getPackage(),
            DesktopFieldAccessHelper9.class.getModule()
        );
        mod.setAccessible(true);
        int m = (int) mod.get(f);
        mod.set(f, m & ~Modifier.FINAL);
      }

      Demodulator.checkAndMakeModuleOpen(
            f.getDeclaringClass().getModule(),
            f.getDeclaringClass().getPackage(),
            DesktopFieldAccessHelper9.class.getModule()
        );
      f.setAccessible(true);

      setters.get(clazz, ObjectMap::new).put(field, lookup.unreflectSetter(f));
      getters.get(clazz, ObjectMap::new).put(field, lookup.unreflectGetter(f));

      initiled.get(clazz).add(field);
    }catch(NoSuchFieldException|IllegalAccessException e){
      throw new RuntimeException(e);
    }
  }

  private Field findField(Class<?> clazz, String field) throws NoSuchFieldException{
    Field f = null;
    Class<?> current = clazz;
    ArrayList<Field> checking = new ArrayList<>();

    while(current != Object.class){
      checking.addAll(Arrays.asList(current.getDeclaredFields()));

      current = current.getSuperclass();
    }

    Optional<Field> opt = checking.stream().filter(fi -> fi.getName().equals(field)).findFirst();
    if(opt.isPresent()){
      f = opt.get();
    }

    if(f == null)
      throw new NoSuchFieldException("no such field \"" + field + "\" found in " + clazz + " and super class!");

    return f;
  }

  @Override
  public void set(Object object, String field, Object value){
    if(useUnsafe){
      initField(object.getClass(), field);
      UnsafeAccess.set(object, fieldPool.get(object.getClass(), EMP_FIEDL).get(field), value);
    }
    else super.set(object, field, value);
  }

  @Override
  public void setStatic(Class<?> clazz, String field, Object value){
    if(useUnsafe){
      initField(clazz, field);
      UnsafeAccess.setStatic(fieldPool.get(clazz, EMP_FIEDL).get(field), value);
    }
    else super.setStatic(clazz, field, value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Object object, String field){
    if(useUnsafe){
      initField(object.getClass(), field);
      return (T) UnsafeAccess.get(object, fieldPool.get(object.getClass(), EMP_FIEDL).get(field));
    }
    return super.get(object, field);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getStatic(Class<?> clazz, String field){
    if(useUnsafe){
      initField(clazz, field);
      return (T) UnsafeAccess.getStatic(fieldPool.get(clazz, EMP_FIEDL).get(field));
    }
    return super.getStatic(clazz, field);
  }
}
