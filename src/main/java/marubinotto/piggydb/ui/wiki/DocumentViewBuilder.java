package marubinotto.piggydb.ui.wiki;

public class DocumentViewBuilder extends HtmlBuilder {

  @Override
  protected String fragmentUrl(Long id, ParseContext context) {
    return context.getWebResources().docViewPath(id);
  }
}
