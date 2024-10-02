package universecore.ui.elements.markdown;

import java.util.ArrayList;
import java.util.List;

public class HighlightMatcher {
  private List<LanguageHighlight> languages = new ArrayList<>();



  public interface LanguageHighlight{
    List<String> splitTokens(String text);
  }
}
