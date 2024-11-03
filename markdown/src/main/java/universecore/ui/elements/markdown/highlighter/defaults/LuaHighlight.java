package universecore.ui.elements.markdown.highlighter.defaults;
import universecore.ui.elements.markdown.highlighter.Capture;
import universecore.ui.elements.markdown.highlighter.PatternsHighlight;
import universecore.ui.elements.markdown.highlighter.SelectionCapture;

import java.util.regex.Pattern;

import static universecore.ui.elements.markdown.highlighter.Scope.Default.*;
/**
 * LuaHighlight
 */
public class LuaHighlight extends DefaultLangDSL{


  static Capture statementCapture(){ 
    return forks(
        //IF
        compound(
            token(KEYWORD, "if"),
            expressionCapture(),
            token(KEYWORD,"then"),
            ifStatementBlockCapture() 
        ),
        //WHILE
        compound(
            token(KEYWORD, "while"),
            expressionCapture(),
            token(KEYWORD,"do"),
            functionStatementBlockCapture() 
        ),
        //REPEAT_UNTIL
        compound(
            token(KEYWORD,"repeat"),
            lazy(LuaHighlight::statementsCapture),
            token(KEYWORD,"until"),
            expressionCapture()
        ),
        compound(
            token(KEYWORD,"for"),
            expressionCapture(),
            token(SEPARATOR,","),
            expressionCapture(),
            compound(
                token(SEPARATOR,","),
                expressionCapture()
                ).setOptional(true),
            token(KEYWORD,"do"),
            functionStatementBlockCapture() 
        ),
        compound(
            token(KEYWORD,"for"),
            makeJoin(
              regex(ARGUMENT, "\\w+"),
              token(SEPARATOR, ",")
            ),
            token(KEYWORD,"in"),
            expressionCapture(),
            token(KEYWORD,"do"),
            functionStatementBlockCapture() 
        )
    );
  }
  static Capture metaExpCapture(){
    return compound(

        forks(
              compound(
                token("("),
                lazy(LuaHighlight::expressionCapture),
                token(")")
            ),
              //LAMBDA
            compound(
                token(KEYWORD, "function"),
                token("("),
                makeJoin(
                  regex(ARGUMENT, "\\w+"),
                  token(SEPARATOR, ",")
                ).setOptional(true),
                token(")"),
                functionStatementBlockCapture()
            ),
            //INVOKE
            compound(
                 forks(
                    regex(KEYWORD, "require"),
                    regex(FUNCTION_INVOKE, "\\w+")
                ),             
                forks(
                compound(
                    token("("),
                    makeJoin(
                        lazy(LuaHighlight::expressionCapture).setOptional(true),
                        token(SEPARATOR, ",")
                    ),
                    token(")")
                ),
                    makeJoin(
                        lazy(LuaHighlight::expressionCapture).setOptional(true),
                        token(SEPARATOR, ",")
                    )
                )
            ),
            constantLiteralCapture(),
            //REF
            regex(VARIABLE, "\\w+")
          )
        );
  }
  static Capture constantLiteralCapture(){
    return forks(
        //STRING
        compound(
            token(STRING, "\""),
            new SelectionCapture(0, Integer.MAX_VALUE,
                regex(
                    CONTROL,
                    "(\\\\[0-7]{3})|(\\\\u[0-9a-fA-F]{4})|(\\\\[0abtnvfre\\\\\"'])"
                ),
                regex(STRING, "[^\"]+")
            ),
            token(STRING, "\"")
        ),
        //CHARACTER
        compound(
            token(STRING, "'"),
            new SelectionCapture(
                regex(
                    CONTROL,
                    "(\\\\[0-7]{3})|(\\\\u[0-9a-fA-F]{4})|(\\\\[0abtnvfre\\\\\"'])"
                ),
                regex(STRING, "[^']")
            ),
            token(STRING, "'")
        ),
        //DOUBLE BRACKETS
        //TODD I can t finish the nesting like [=[abc [[bcd]] abd]=]
        compound(
            token(2, "["),
            new SelectionCapture(0, Integer.MAX_VALUE,
                regex(
                    CONTROL,
                    "(\\\\[0-7]{3})|(\\\\u[0-9a-fA-F]{4})|(\\\\[0abtnvfre\\\\\"'])"
                ),
                regex(STRING, "[^\\]]")
            ),
            token(2, "]")
        ),
        //NUMBER
        //TODD
        //BOOLEAN
        regex(KEYWORD, "true|false"),
        //NULL
        token(KEYWORD, "nil")
    );
  }
static Capture statementsCapture(){
    return compound(0, Integer.MAX_VALUE,
        statementCapture()
    );
  }
static Capture functionStatementBlockCapture() {
       return compound(
            lazy(LuaHighlight::statementsCapture),
            token(KEYWORD,"end")
        );
  }
static Capture ifStatementBlockCapture() {
       return compound(
            lazy(LuaHighlight::statementsCapture),
            forks(
                token(KEYWORD,"end"),
                compound(
                    token(KEYWORD,"else"),
                    lazy(LuaHighlight::statementsCapture),
                    token(KEYWORD,"end")
                    )
                ),
                compound(
                    token(KEYWORD,"elseif"),
                    ifStatementBlockCapture()
                )
        );
  }
static Capture statementBlockCapture() {
    return forks(
        compound(
            token(KEYWORD,"do"),
            lazy(LuaHighlight::statementsCapture),
            token(KEYWORD,"end")
        ),
        lazy(LuaHighlight::statementCapture)
    ).setOptional(true);
  }
  static Capture expressionCapture(){
    return makeJoin(
        compound(
            metaExpCapture(),
            regex(OPERATOR, "\\+\\+|--").setOptional(true)
        ),
        regex(OPERATOR, "(->|!=|==|<=|>=|&&|\\|\\||\\+=|-=|\\*=|/=|%=|&=|\\|=|\\^=|<<=|>>=|>>>=)|[=.+\\-*/%&|<>^]")
    );
  }
  public static PatternsHighlight create(){
    
    PatternsHighlight res = new PatternsHighlight("lua");
    res.tokensSplit = Pattern.compile("\\s+");
    res.rawTokenMatcher = Pattern.compile("//.*|/\\*(\\s|.)*?\\*/");
    res.symbolMatcher = Pattern.compile("(->|!=|==|<=|>=|&&|\\|\\||\\+\\+|--|\\+=|-=|\\*=|/=|%=|&=|\\|=|\\^=|<<=|>>=|>>>=)|(\\\\[0-7]{3})|(\\\\u[0-9a-fA-F]{4})|(\\\\[0abtnvfre\\\\\"'])|[\\\\.+\\-*/%&|!<>~^=,;:(){}\"'\\[\\]]");
    res//RAW CONTEXT
        .addRawContextPattern("line_comment", block(COMMENT,
            of(token(2, "-")),
            of(line(COMMENT))
        ))
        .addRawContextPattern("block_comment", block(COMMENT,
            of(token(2,"-"), token(2, "[")),
            of(token(2,"]"))
        ))

        .addPattern("keywords", serial(-100, token(
            KEYWORD,
            "local", "const",
            "return", "require", 
            "do", "then", "while", "for", "in", "break", 
            "if", "else", "elseif", "goto", "until", "repeat",
            "and", "or", "not",
            "nil", "true", "false"
        )))
        ;

    return res;
  }
}
