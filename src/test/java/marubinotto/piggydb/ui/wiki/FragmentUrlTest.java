package marubinotto.piggydb.ui.wiki;

import static marubinotto.piggydb.fixture.EntityFixtures.newFragmentWithTitle;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.fixture.mock.WikiParserMock;
import marubinotto.piggydb.impl.InMemoryDatabase;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.ui.wiki.DocumentBuilder;
import marubinotto.piggydb.ui.wiki.FragmentUrl;
import marubinotto.piggydb.ui.wiki.ParseContext;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

public class FragmentUrlTest {

	private DocumentBuilder builder = createMock(DocumentBuilder.class);
	
	private FragmentRepository fragmentRepository = 
		new InMemoryDatabase().getFragmentRepository();
	
	private ParseContext context = new ParseContext(
		new WikiParserMock(), 
		new User(), 
		this.fragmentRepository, 
		this.fragmentRepository.getTagRepository());
	
	private Long id1;
	private Long id2;
	private Long id3;
	
	@Before
	public void given() throws Exception {
		this.id1 = this.fragmentRepository.register(newFragmentWithTitle("private"));
		this.id2 = this.fragmentRepository.register(new RawFragment());
		
		RawFragment fragment = newFragmentWithTitle("public");
		fragment.getMutableClassification().addTag(new RawTag(Tag.NAME_PUBLIC));
		this.id3 = this.fragmentRepository.register(fragment);
	}
	
	@Test
	public void missingId() throws Exception {
		FragmentUrl fragmentUrl = new FragmentUrl("fragment:");		
		assertEquals("fragment:", fragmentUrl.toMarkup(this.builder, this.context));
	}
	
	@Test
	public void invalidId() throws Exception {
		FragmentUrl fragmentUrl = new FragmentUrl("fragment:xxx");
		assertEquals("fragment:xxx", fragmentUrl.toMarkup(this.builder, this.context));
	}
	
	private String execute(String url) throws Exception {
		replay(this.builder);
		
		FragmentUrl fragmentUrl = new FragmentUrl(url);
		String result = fragmentUrl.toMarkup(this.builder, this.context);
		
		verify(this.builder);
		return result;
	}
	
	@Test
	public void putLinkToFragment() throws Exception {
		String url = "fragment:999";

		expect(
			this.builder.makeLinkToFragment(this.context, 999L, url))
				.andReturn("return-by-builder");

		assertEquals("return-by-builder", execute(url));
	}
	
	@Test
	public void putLinkToFragmentWithTitle() throws Exception {
		String url = "fragment:" + this.id1 + ":title";
		
		expect(
			this.builder.makeLinkToFragment(this.context, this.id1, "private"))
				.andReturn("return-by-builder");

		assertEquals("return-by-builder", execute(url));
	}
		
	@Test
	public void putLinkToFragmentWithNullTitle() throws Exception {
		String url = "fragment:" + this.id2 + ":title";
		
		expect(
			this.builder.makeLinkToFragment(this.context, this.id2, "[No title]"))
				.andReturn("return-by-builder");

		assertEquals("return-by-builder", execute(url));
	}
	
	@Test
	public void putLinkToFragmentWithDetail() throws Exception {
		String url = "fragment:" + this.id1 + ":detail";
		
		Capture<Fragment> arg = new Capture<Fragment>();
		expect(
			this.builder.makeLinkToFragmentWithDetail(eq(this.context), capture(arg)))
				.andReturn("return-by-builder");

		assertEquals("return-by-builder", execute(url));
		assertEquals(this.id1, arg.getValue().getId());
	}
	
	@Test
	public void embedFragment() throws Exception {
		String url = "fragment:" + this.id1 + ":embed";
		
		Capture<Fragment> arg = new Capture<Fragment>();
		expect(
			this.builder.makeEmbeddedFragment(eq(this.context), capture(arg)))
				.andReturn("return-by-builder");
		
		assertEquals("return-by-builder", execute(url));
		assertEquals(this.id1, arg.getValue().getId());
	}
	
	@Test
	public void embedFragmentReturnsNull() throws Exception {
		String url = "fragment:" + this.id1 + ":embed";
		
		Capture<Fragment> arg = new Capture<Fragment>();
		expect(
			this.builder.makeEmbeddedFragment(eq(this.context), capture(arg)))
				.andReturn(null);
		
		assertEquals(url, execute(url));
	}
	
	private void initContextWithoutAuth() {
		this.context = new ParseContext(
			new WikiParserMock(), 
			null, 
			this.fragmentRepository, 
			this.fragmentRepository.getTagRepository());
	}
	
	@Test
	public void embedPrivateFragmentWithoutAuth() throws Exception {
		initContextWithoutAuth();
		String url = "fragment:" + this.id1 + ":embed";
		
		assertEquals(url, execute(url));
	}
	
	@Test
	public void embedPublicFragmentWithoutAuth() throws Exception {
		initContextWithoutAuth();
		String url = "fragment:" + this.id3 + ":embed";
		
		Capture<Fragment> arg = new Capture<Fragment>();
		expect(
			this.builder.makeEmbeddedFragment(eq(this.context), capture(arg)))
				.andReturn("return-by-builder");
		
		assertEquals("return-by-builder", execute(url));
		assertEquals(this.id3, arg.getValue().getId());
	}
}
