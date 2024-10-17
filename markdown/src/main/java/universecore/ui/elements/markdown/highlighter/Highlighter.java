package universecore.ui.elements.markdown.highlighter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Highlighter {
  private final Map<String, LanguageHighlight<?>> languages = new HashMap<>();

  public Highlighter addLanguage(LanguageHighlight<?> highlight){
    languages.put(highlight.language().toLowerCase(), highlight);

    return this;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public TokensContext analyze(String language, String text){
    LanguageHighlight highlight = languages.get(language.toLowerCase());

    if (highlight == null) return null;

    TokensContext context = highlight.initContext();
    highlight.splitTokens(context, text);

    List<Token> tokens = context.getTokens();

    context.resetCursor();
    while(context.currCursor() < tokens.size()){
      Token token = tokens.get(context.currCursor());

      int step = highlight.flowScope(context, token);

      if (step <= 0) context.forwardCursor(1);
      else context.forwardCursor(step);
    }

    context.getTokensRaw().forEach(e -> {
      if (e.scope == null) {
        e.scope = Scope.Default.NONE;
      }
    });

    return context;
  }
}
