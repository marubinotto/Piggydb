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
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.model.entity.RawTag;
import marubinotto.piggydb.ui.wiki.DocumentBuilder;
import marubinotto.piggydb.ui.wiki.FragmentUrn;
import marubinotto.piggydb.ui.wiki.ParseContext;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

public class FragmentUrnTest {

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
		FragmentUrn fragmentUrn = new FragmentUrn("fragment:");		
		assertEquals("fragment:", fragmentUrn.toMarkup(this.builder, this.context));
	}
	
	@Test
	public void invalidId() throws Exception {
		FragmentUrn fragmentUrn = new FragmentUrn("fragment:xxx");
		assertEquals("fragment:xxx", fragmentUrn.toMarkup(this.builder, this.context));
	}
	
	private String execute(String urn) throws Exception {
		replay(this.builder);
		
		FragmentUrn fragmentUrn = new FragmentUrn(urn);
		String result = fragmentUrn.toMarkup(this.builder, this.context);
		
		verify(this.builder);
		return result;
	}
	
	@Test
	public void putLinkToFragment() throws Exception {
		String urn = "fragment:999";

		expect(
			this.builder.makeLinkToFragment(this.context, 999L, urn))
				.andReturn("return-by-builder");

		assertEquals("return-by-builder", execute(urn));
	}
	
	@Test
	public void putLinkToFragmentWithTitle() throws Exception {
		String urn = "fragment:" + this.id1 + ":title";
		
		expect(
			this.builder.makeLinkToFragment(this.context, this.id1, "private"))
				.andReturn("return-by-builder");

		assertEquals("return-by-builder", execute(urn));
	}
		
	@Test
	public void putLinkToFragmentWithNullTitle() throws Exception {
		String urn = "fragment:" + this.id2 + ":title";
		
		expect(
			this.builder.makeLinkToFragment(this.context, this.id2, "[No title]"))
				.andReturn("return-by-builder");

		assertEquals("return-by-builder", execute(urn));
	}
	
	@Test
	public void putLinkToFragmentWithDetail() throws Exception {
		String urn = "fragment:" + this.id1 + ":detail";
		
		Capture<Fragment> arg = new Capture<Fragment>();
		expect(
			this.builder.makeLinkToFragmentWithDetail(eq(this.context), capture(arg)))
				.andReturn("return-by-builder");

		assertEquals("return-by-builder", execute(urn));
		assertEquals(this.id1, arg.getValue().getId());
	}
	
	@Test
	public void embedFragment() throws Exception {
		String urn = "fragment:" + this.id1 + ":embed";
		
		Capture<Fragment> arg = new Capture<Fragment>();
		expect(
			this.builder.makeEmbeddedFragment(eq(this.context), capture(arg)))
				.andReturn("return-by-builder");
		
		assertEquals("return-by-builder", execute(urn));
		assertEquals(this.id1, arg.getValue().getId());
	}
	
	@Test
	public void embedFragmentReturnsNull() throws Exception {
		String urn = "fragment:" + this.id1 + ":embed";
		
		Capture<Fragment> arg = new Capture<Fragment>();
		expect(
			this.builder.makeEmbeddedFragment(eq(this.context), capture(arg)))
				.andReturn(null);
		
		assertEquals(urn, execute(urn));
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
		String urn = "fragment:" + this.id1 + ":embed";
		
		assertEquals(urn, execute(urn));
	}
	
	@Test
	public void embedPublicFragmentWithoutAuth() throws Exception {
		initContextWithoutAuth();
		String urn = "fragment:" + this.id3 + ":embed";
		
		Capture<Fragment> arg = new Capture<Fragment>();
		expect(
			this.builder.makeEmbeddedFragment(eq(this.context), capture(arg)))
				.andReturn("return-by-builder");
		
		assertEquals("return-by-builder", execute(urn));
		assertEquals(this.id3, arg.getValue().getId());
	}
}
