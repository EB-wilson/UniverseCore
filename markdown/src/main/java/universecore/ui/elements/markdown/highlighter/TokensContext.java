package universecore.ui.elements.markdown.highlighter;

import java.util.List;

public interface TokensContext {
  List<Token> getTokens();
  List<Token> getTokensRaw();

  Token getToken(int index);
  Token getTokenRaw(int index);

  void putToken(Token tokens);
  void putTokenRaw(Token tokens);

  int getTokenCount();
  int getTokenCountRaw();

  default void applyScopes(ScopeHandler handler){
    for (Token token : getTokensRaw()) {
      if (token.scope != null) token.scope.apply(token, handler);
    }
  }

  int currCursor();
  void resetCursor();
  void forwardCursor(int step);
}
