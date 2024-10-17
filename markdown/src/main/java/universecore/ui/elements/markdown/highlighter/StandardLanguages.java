package universecore.ui.elements.markdown.highlighter;

import universecore.ui.elements.markdown.highlighter.defaults.JavaHighlight;

public class StandardLanguages {
  public static PatternsHighlight JAVA;

  static {
    JAVA = JavaHighlight.create();
  }
}
