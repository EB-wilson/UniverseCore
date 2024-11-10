import universecore.ui.elements.markdown.highlighter.Highlighter;
import universecore.ui.elements.markdown.highlighter.Scope;
import universecore.ui.elements.markdown.highlighter.StandardLanguages;
import universecore.ui.elements.markdown.highlighter.TokensContext;

/**
 * LuaTest
 */
public class LuaTest {

  
  public static final String CODE = """
--test
--[[test
abd]]
   import("core.test") 
   local str="abc",str2= [=[abcte[[ee]]eee]=]
   function test(a)
     if a == true then
       self.test2()
       self:test3()
       return true
      end
   end
   function obj:test(a,b)

     end
   local obj={abc=0,init=function(a) end}
    print "test"
    require "test"
    for i,k in ipairs(array) do
    end

    """;
  public static void main(String...strings){
    Highlighter matcher = new Highlighter();
    matcher.addLanguage(StandardLanguages.LUA);

    long time = System.currentTimeMillis();
    TokensContext tokens = matcher.analyze("lua", CODE);
    long delta = System.currentTimeMillis() - time;

    System.out.println("Time: " + delta + "ms");

    tokens.getTokens().forEach(token -> {
      System.out.println(token.text + " " + token.scope);
    });

    tokens.applyScopes((token, scope) -> {
      if (scope instanceof Scope.Default def){
        String color = switch (def) {
          case NONE, OPERATOR, SPACE, CODE_BLOCK -> "\033[0m";
          case KEYWORD, CONTROL, SEPARATOR -> "\u001b[38;2;204;120;50m";
          case FUNCTION_INVOKE -> "\u001b[38;2;97;154;195m";
          case VARIABLE -> "\u001b[38;2;248;223;114m";
          case ARGUMENT -> "\u001b[38;2;147;181;207m";
          case NUMBER -> "\u001b[38;2;104;151;187m";
          case MEMBER_VAR -> "\u001b[38;2;152;118;170m";
          case FUNCTION, CONSTRUCTOR -> "\u001b[38;2;49;112;167m";
          case TYPE -> "\u001b[38;2;190;112;50m";
          case TYPE_ARG -> "\u001b[38;2;80;120;116m";
          case STRING -> "\u001b[38;2;106;135;89m";
          case COMMENT -> "\u001b[38;2;128;128;0m";
          case DOCS -> "\u001b[38;2;98;151;85m";
          case DOC_MARK -> "\u001b[38;2;98;151;85m\033[1m";
          case ANNOTATION -> "\u001b[38;2;187;181;41m";
        };

        System.out.print(color + token.text);
      }
      else System.out.print(token.text);
    });
    System.out.print("\033[0m");
  }

}
