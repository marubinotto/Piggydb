package marubinotto.piggydb.ui.wiki;

public class DocumentViewBuilder extends HtmlBuilder {

  @Override
  protected String fragmentUrl(Long id, ParseContext context) {
    return context.getWebResources().docViewPath(id);
  }
  
  @Override
  public String processTagName(ParseContext context, String tagName) {
    String url = context.getWebResources().docViewPath(tagName);
    return "<a class=\"tag\" href=\"" + url + "\">" + tagName + "</a>";
  }
}
