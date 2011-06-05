package marubinotto.piggydb.presentation.wiki.html;

import java.io.StringWriter;
import java.util.Stack;

import marubinotto.piggydb.fixture.mock.WikiParserMock;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.repository.FragmentRepositoryRI;
import marubinotto.piggydb.presentation.page.WebResources;
import marubinotto.piggydb.presentation.wiki.HtmlBuilder;
import marubinotto.piggydb.presentation.wiki.ParseContext;

import org.junit.Before;

public abstract class HtmlBuilderTestBase {

	protected HtmlBuilder object = new HtmlBuilder();

	protected ParseContext context;
	protected StringWriter output = new StringWriter();
	protected WikiParserMock wikiParserMock = new WikiParserMock();
	protected User user = new User();
	protected FragmentRepositoryRI fragmentRepository = new FragmentRepositoryRI();

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
