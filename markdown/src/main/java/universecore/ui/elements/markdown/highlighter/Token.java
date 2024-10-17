package universecore.ui.elements.markdown.highlighter;

public class Token {
  public final String text;

  public Scope scope;
  public Object data;

  public int index;
  public int rawIndex;

  public Token(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return "{\"" + text + "\": " + scope + "}";
  }
}
