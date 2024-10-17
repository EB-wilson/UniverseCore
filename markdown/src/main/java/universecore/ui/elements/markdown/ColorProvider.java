package universecore.ui.elements.markdown;

import arc.graphics.Color;
import universecore.ui.elements.markdown.highlighter.Scope;

import java.util.HashMap;
import java.util.Map;

public class ColorProvider {
  public Color defaultColor = Color.white;

  private final Map<String, ColorMap> languages = new HashMap<>();

  public Color getColor(String language, Scope scope){
    ColorMap map = languages.get(language);

    if (map == null) return defaultColor;

    return map.colorMap.getOrDefault(scope, defaultColor);
  }

  public ColorMap createMap(String language){
    ColorMap map = new ColorMap();
    languages.put(language.toLowerCase(), map);

    return map;
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
