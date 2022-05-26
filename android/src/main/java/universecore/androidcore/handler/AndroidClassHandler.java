package universecore.androidcore.handler;


import mindustry.Vars;
import universecore.androidcore.classes.AndroidGeneratedClassLoader;
import universecore.util.classes.BaseDynamicClassLoader;
import universecore.androidcore.classes.DexLoaderFactory;
import universecore.util.classes.JarList;
import universecore.util.handler.AbstractClassHandler;
import universecore.androidcore.proxy.AndroidProxy;
import universecore.util.proxy.IProxy;

import java.util.HashMap;

/**类处理器的平台实现类，在UncCore中保存了一个默认实例，或者单独实例化使用
 * @author EBwilson
 * @since 1.2*/
public class AndroidClassHandler extends AbstractClassHandler{
  static final BaseDynamicClassLoader dynamicLoader = DexLoaderFactory.generateClassLoader(Vars.mods.mainLoader());
  
  public AndroidClassHandler(JarList.ModInfo callerMod, ClassLoader modLoader) throws ClassNotFoundException, NoSuchMethodException{
    super(callerMod, new AndroidGeneratedClassLoader(callerMod, jarList.getCacheFile(callerMod).file(), Vars.mods.mainLoader()), modLoader);
  }
  
  /**用于获取目标类型的代理容器的工厂方法，代理子目标有一唯一限定名称，用于在所有此类型的代理容器中索引唯一目标
   * @param clazz 要代理的目标类型
   * @param name 代理容器的唯一限定名称
   * @return 代理容器*/
  @Override
  @SuppressWarnings("unchecked")
  public <T> IProxy<T> getProxy(Class<T> clazz, String name){
    return (IProxy<T>) proxies.computeIfAbsent(clazz, e -> new HashMap<>())
        .computeIfAbsent(name, e -> new AndroidProxy<>(clazz, getCurrentLoader(), this));
  }
  
  @Override
  public BaseDynamicClassLoader getDynamicLoader(){
    return dynamicLoader;
  }
}
