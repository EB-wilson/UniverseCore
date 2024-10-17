package universecore.ui.elements.markdown.highlighter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class MatcherContext implements TokensContext{
  private final Stack<Block> blockStack = new Stack<>();
  private final List<Token> tokens = new ArrayList<>();
  private final List<Token> rawTokens = new ArrayList<>();

  private int currentIndex = 0;

  public void pushBlock(Block block){
    blockStack.push(block);
  }

  public Block peekBlock(){
    return blockStack.isEmpty()? null: blockStack.peek();
  }

  public void popBlock(){
    blockStack.pop();
  }

  public int blockDepth(){
    return blockStack.size();
  }

  public MatcherContext subContext(){
    MatcherContext res = new MatcherContext();
    res.currentIndex = currentIndex;
    res.tokens.addAll(tokens);
    res.rawTokens.addAll(rawTokens);
    return res;
  }

  @Override
  public List<Token> getTokens() {
    return Collections.unmodifiableList(tokens);
  }

  @Override
  public List<Token> getTokensRaw() {
    return Collections.unmodifiableList(rawTokens);
  }

  @Override
  public void putToken(Token tokens) {
    tokens.index = this.tokens.size();
    tokens.rawIndex = this.rawTokens.size();

    this.tokens.add(tokens);
    this.rawTokens.add(tokens);
  }

  @Override
  public void putTokenRaw(Token tokens) {
    tokens.index = this.tokens.size();
    tokens.rawIndex = this.rawTokens.size();
    this.rawTokens.add(tokens);
  }

  @Override
  public int getTokenCount() {
    return tokens.size();
  }

  @Override
  public int getTokenCountRaw() {
    return rawTokens.size();
  }

  @Override
  public Token getToken(int index) {
    return tokens.get(index);
  }

  @Override
  public Token getTokenRaw(int index) {
    return rawTokens.get(index);
  }

  @Override
  public int currCursor() {
    return currentIndex;
  }

  @Override
  public void resetCursor() {
    currentIndex = 0;
  }

  @Override
  public void forwardCursor(int step) {
    currentIndex += step;
  }
}
