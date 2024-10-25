package universecore.ui.elements.markdown;

import arc.graphics.Color;
import universecore.ui.elements.markdown.highlighter.Scope;

import java.util.HashMap;
import java.util.Map;

public class ColorProvider {
  public Color defaultColor = Color.white;

  private final Map<String, ColorMap> languages = new HashMap<>();

  /**获取指定语言为scope分配的颜色*/
  public Color getColor(String language, Scope scope){
    ColorMap map = languages.get(language);

    if (map == null) return defaultColor;

    return map.colorMap.getOrDefault(scope, defaultColor);
  }

  /**创建一个颜色表，添加并返回它，如果这个语言色表已经存在将被新的表覆盖*/
  public ColorMap createMap(String language){
    ColorMap map = new ColorMap();
    languages.put(language.toLowerCase(), map);

    return map;
  }

  /**获取一个现有的颜色表，如果颜色表不存在会返回null*/
  public ColorMap getMap(String language){
    return languages.get(language.toLowerCase());
  }

  public static class ColorMap{
    private final Map<Scope, Color> colorMap = new HashMap<>();

    public ColorMap put(Color color, Scope... scopes){
      for (Scope scope : scopes) {
        colorMap.put(scope, color);
      }
      return this;
    }
  }
}
