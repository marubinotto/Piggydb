package marubinotto.piggydb.ui.wiki;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Stack;

import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.ui.page.common.HtmlFragments;
import marubinotto.piggydb.ui.page.common.WebResourcePaths;
import marubinotto.util.Assert;
import marubinotto.util.ThreadLocalCache;

import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;

public class ParseContext {

  // fragment scope
  private PrintWriter writer;
  private Stack<Object> blockStack = new Stack<Object>();
  private PatternMatcher matcher = ThreadLocalCache.get(Perl5Matcher.class);

  // parser scope
  private WikiParser wikiParser;
  private Stack<Long> fragmentStack;

  // login session
  private User user;

  // global
  private WebResourcePaths webResources;
  private HtmlFragments htmlFragments;
  private FragmentRepository fragmentRepository;
  private TagRepository tagRepository;

  public ParseContext(
    Writer writer, 
    WikiParser wikiParser, 
    Stack<Long> fragmentStack, 
    User user, 
    WebResourcePaths webResources,
    FragmentRepository fragmentRepository, 
    TagRepository tagRepository) {
    
    Assert.Arg.notNull(writer, "writer");
    Assert.Arg.notNull(wikiParser, "wikiParser");
    Assert.Arg.notNull(webResources, "webResources");
    Assert.Arg.notNull(fragmentRepository, "fragmentRepository");
    Assert.Arg.notNull(tagRepository, "tagRepository");

    this.writer = new PrintWriter(writer);

    this.wikiParser = wikiParser;
    this.fragmentStack = fragmentStack;

    this.user = user;

    this.webResources = webResources;
    this.htmlFragments = new HtmlFragments(this.webResources);
    this.fragmentRepository = fragmentRepository;
    this.tagRepository = tagRepository;
  }

  // for test
  public ParseContext(
    WikiParser wikiParser, 
    User user, 
    FragmentRepository fragmentRepository, 
    TagRepository tagRepository) {
    
    this(new StringWriter(), 
      wikiParser, 
      new Stack<Long>(), 
      user, 
      new WebResourcePaths("", "test"), 
      fragmentRepository, 
      tagRepository);
  }

  public void print(String output) {
    this.writer.print(output);
  }

  public void println(String output) {
    this.writer.println(output);
  }

  public void println() {
    this.writer.println();
  }

  public Stack<Object> getBlockStack() {
    return this.blockStack;
  }

  public PatternMatcher getMatcher() {
    return this.matcher;
  }

  public WikiParser getWikiParser() {
    return this.wikiParser;
  }

  public Stack<Long> getFragmentStack() {
    return this.fragmentStack;
  }

  public boolean isAuthenticated() {
    return this.user != null;
  }

  public User getUser() {
    return this.user;
  }

  public WebResourcePaths getWebResources() {
    return this.webResources;
  }

  public HtmlFragments getHtmlFragments() {
    return htmlFragments;
  }

  public FragmentRepository getFragmentRepository() {
    return this.fragmentRepository;
  }

  public TagRepository getTagRepository() {
    return this.tagRepository;
  }
}
