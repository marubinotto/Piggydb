package marubinotto.piggydb.ui.page.common;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.ui.wiki.DefaultWikiParser;
import marubinotto.piggydb.ui.wiki.WikiParser;
import marubinotto.util.Size;
import marubinotto.util.time.DateTime;

import org.junit.Test;

public class HtmlFragmentsTest {

  private HtmlFragments object = new HtmlFragments(new WebResourcePaths("/context", "1.0"));
  
  @Test
  public void cssImport() throws Exception {
    String result = this.object.cssImport("style/prettify.css", true, null);
    assertEquals(
      "<link rel=\"stylesheet\" type=\"text/css\" href=\"/context/style/prettify.css?1.0\"/>", 
      result);
  }
  
  @Test
  public void cssImportWithMedia() throws Exception {
    String result = this.object.cssImport("style/piggydb-print.css", true, "print");
    assertEquals(
      "<link rel=\"stylesheet\" type=\"text/css\" href=\"/context/style/piggydb-print.css?1.0\" media=\"print\"/>", 
      result);
  }
  
  @Test
  public void jsImport() throws Exception {
    String result = this.object.jsImport("scripts/marubinotto.js", true);
    assertEquals(
      "<script type=\"text/javascript\" src=\"/context/scripts/marubinotto.js?1.0\"></script>", 
      result);
  }
  
  @Test
  public void fragmentImage() throws Exception {
    RawFragment fragment = new RawFragment();
    fragment.setId(1L);
    fragment.setUpdateDatetime(new DateTime(12345L));
    
    String result = this.object.fragmentImage(fragment);
    assertTrue(result, result.contains("/context/command/get-file.htm?id=1&t=12345"));
  }
  
  @Test
  public void fileIcon() throws Exception {
    String result = this.object.fileIcon("xls");
    assertTrue(result.contains("/context/images/file-icons/xls.png"));
  }
  
  @Test
  public void fileIconWithNullFileType() throws Exception {
    String result = this.object.fileIcon(null);
    assertTrue(result.contains("/context/images/file-icons/default.png"));
  }
  
  @Test
  public void linkToFragment() throws Exception {
    String result = this.object.linkToFragment(1);
    assertEquals("<a href=\"/context/fragment.htm?id=1\">#1</a>", result);
  }
  
  @Test
  public void linkToFragmentFileWithSize() throws Exception {
    RawFragment fragment = new RawFragment();
    fragment.setId(1L);
    fragment.setFileName("file.txt");
    fragment.setFileSize(new Size(1000000));
    
    String result = this.object.linkToFragmentFileWithSize(fragment);
    
    assertTrue(result.contains("/context/command/get-file.htm?id=1"));
    assertTrue(result.contains("file.txt"));
    assertTrue(result.contains("(976.56 KByte)"));
  }
  
  @Test
  public void preformattedContent() throws Exception {
    User user = new User();
    
    InMemoryDatabase database = new InMemoryDatabase();
    WikiParser wikiParser = new DefaultWikiParser();
    wikiParser.setFragmentRepository(database.getFragmentRepository());
    wikiParser.setTagRepository(database.getTagRepository());
    
    Fragment fragment = database.getFragmentRepository().newInstance(user);
    fragment.setContentByUser("1 + 1 = 2", user);
    
    // with a user
    String result = this.object.preformattedContent(fragment, wikiParser, user);
    String expected = "<pre class=\"pre-fragment\">1 + 1 = 2\n</pre>";
    assertEquals(expected, result);
    
    // without a user
    result = this.object.preformattedContent(fragment, wikiParser, null);
    assertEquals(expected, result);
  }
}
