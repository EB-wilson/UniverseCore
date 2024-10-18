package universecore.ui.elements.markdown.highlighter;

public interface Scope {
  void apply(Token token, ScopeHandler handler);

  enum Default implements Scope {
    //internal special
    NONE,
    SPACE,

    KEYWORD,
    OPERATOR,
    NUMBER,
    TYPE,
    TYPE_ARG,
    STRING,
    CONTROL,
    COMMENT,
    DOCS,
    DOC_MARK,
    SEPARATOR,
    VARIABLE,
    MEMBER_VAR,
    FUNCTION,
    CONSTRUCTOR,
    FUNCTION_INVOKE,
    ARGUMENT,
    CODE_BLOCK,
    ANNOTATION;

    @Override
    public void apply(Token token, ScopeHandler handler) {
      handler.applyScope(token, this);
    }
  }
}
