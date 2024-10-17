package universecore.ui.elements.markdown.highlighter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternsHighlight implements LanguageHighlight<MatcherContext>, NameIndexer<TokenMatcher> {
  private final String language;
  private final Map<String, TokenMatcher> matchers = new LinkedHashMap<>();

  public Pattern tokensSplit = Pattern.compile("\\s+");
  public Pattern symbolMatcher = Pattern.compile("[\\\\.+\\-*/%&|!<>~^=,;:(){}\"'\\[\\]]");

  public PatternsHighlight(String language) {
    this.language = language;
  }

  public PatternsHighlight addPattern(String patternName, TokenMatcher matcher) {
    this.matchers.put(patternName, matcher.create());

    return this;
  }

  @Override
  public List<TokenMatcher> indexes(String... names) {
    List<TokenMatcher> list = new ArrayList<>();
    Set<String> set = new HashSet<>(Arrays.asList(names));
    for (Map.Entry<String, TokenMatcher> entry : matchers.entrySet()) {
      if (set.contains(entry.getKey())) list.add(entry.getValue());
    }
    return list;
  }

  @Override
  public List<TokenMatcher> allIndexed() {
    return new ArrayList<>(matchers.values());
  }

  @Override
  public String language() {
    return language;
  }

  @Override
  public MatcherContext initContext() {
    MatcherContext res = new MatcherContext();

    List<TokenMatcher> list = new ArrayList<>();
    for (TokenMatcher tokenMatcher : matchers.values()) {
      list.add(tokenMatcher.create());
    }
    res.pushBlock(new Block(null, list));

    return res;
  }

  @Override
  public void splitTokens(TokensContext context, String text) {
    Matcher sepMatcher = tokensSplit.matcher(text);

    int lastEnd = 0;
    while(sepMatcher.find()){
      if (lastEnd != sepMatcher.start()){
        String split = text.substring(lastEnd, sepMatcher.start());

        splitSymbols(context, split);
      }

      lastEnd = sepMatcher.end();

      String sep = sepMatcher.group();
      Token token = new Token(sep);
      token.scope = Scope.Default.SPACE;
      context.putTokenRaw(token);
    }

    if (lastEnd != text.length()){
      String split = text.substring(lastEnd);

      splitSymbols(context, split);
    }
  }

  private void splitSymbols(TokensContext context, String str){
    Matcher matcher = symbolMatcher.matcher(str);
    int lastEnd = 0;

    while (matcher.find()) {
      int matchStart = matcher.start();

      if (matchStart != lastEnd) {
        Token newToken = new Token(str.substring(lastEnd, matchStart));
        context.putToken(newToken);
      }

      lastEnd = matcher.end();

      Token newToken = new Token(matcher.group());
      context.putToken(newToken);
    }

    if (lastEnd != str.length()) {
      Token newToken = new Token(str.substring(lastEnd));
      context.putToken(newToken);
    }
  }

  @Override
  public int flowScope(MatcherContext context, Token token) {
    Block block = context.peekBlock();
    Scope scope = block.scope();
    if (scope != null) token.scope = scope;

    List<TokenMatcher> mats = block.matchers();
    mats.sort(TokenMatcher::compareTo);

    for (TokenMatcher matcher : mats) {
      try {
        int n = matcher.match(context, token);
        matcher.apply(context, token);

        return n;
      } catch (TokenMatcher.MatchFailed ignored) {}
    }

    return 1;
  }
}
