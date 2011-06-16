package marubinotto.piggydb.ui.wiki.html;

import java.io.StringWriter;
import java.util.Stack;

import marubinotto.piggydb.fixture.mock.WikiParserMock;
import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.ui.page.WebResources;
import marubinotto.piggydb.ui.wiki.HtmlBuilder;
import marubinotto.piggydb.ui.wiki.ParseContext;

import org.junit.Before;

public abstract class HtmlBuilderTestBase {

	protected HtmlBuilder object = new HtmlBuilder();

	protected ParseContext context;
	protected StringWriter output = new StringWriter();
	protected WikiParserMock wikiParserMock = new WikiParserMock();
	protected User user = new User();
	protected FragmentRepository fragmentRepository = 
		new InMemoryDatabase().getFragmentRepository();

	@Before
	public void given() throws Exception {
		this.wikiParserMock.setFragmentRepository(this.fragmentRepository);
		this.wikiParserMock.setTagRepository(this.fragmentRepository.getTagRepository());
		
		this.context = new ParseContext(
			this.output, 
			this.wikiParserMock,
			new Stack<Long>(),
			this.user,
			new WebResources("", "test"), 
			this.fragmentRepository, 
			this.fragmentRepository.getTagRepository());
	}
}
