package universecore.ui.elements.markdown;

import org.commonmark.Extension;
import org.commonmark.ext.ins.internal.InsDelimiterProcessor;
import org.commonmark.parser.Parser;

public class CurtainExtension implements Parser.ParserExtension{
  private CurtainExtension() {
  }

  public static Extension create() {
    return new CurtainExtension();
  }

  @Override
  public void extend(Parser.Builder parserBuilder) {
    parserBuilder.customDelimiterProcessor(new CurtainDelimiterProcessor());
  }
}
