package marubinotto.piggydb.presentation.page.model;

import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.MutableClassification;
import marubinotto.piggydb.model.entity.RawTag;

import org.junit.Before;
import org.junit.Test;

public class FragmentTagsTest {

	private FragmentTags object = new FragmentTags();
	private MutableClassification fragmentTags = new MutableClassification();
	
	@Before
	public void given() throws Exception {
		this.fragmentTags.addTag(new RawTag("web"));
		this.fragmentTags.addTag(new RawTag("java"));
		this.fragmentTags.addTag(new RawTag("knowledge"));
	}
	
	@Test
	public void contextIsNull() throws Exception {
		FragmentTags result = this.object.newInstance(this.fragmentTags, null);
		
		assertEquals(3, result.toShow.size());
		assertEquals("java", result.toShow.get(0).getName());
		assertEquals("knowledge", result.toShow.get(1).getName());
		assertEquals("web", result.toShow.get(2).getName());
		
		assertEquals(0, result.toHide.size());
	}
	
	@Test
	public void oneContextMatch() throws Exception {
		MutableClassification context = new MutableClassification();
		context.addTag(new RawTag("web"));
		
		FragmentTags result = this.object.newInstance(this.fragmentTags, context);
		
		assertEquals(2, result.toShow.size());
		assertEquals("java", result.toShow.get(0).getName());
		assertEquals("knowledge", result.toShow.get(1).getName());
		
		assertEquals(1, result.toHide.size());
		assertEquals("web", result.toHide.get(0).getName());
	}
	
	@Test
	public void allInContext() throws Exception {
		MutableClassification context = new MutableClassification();
		context.addTag(new RawTag("web"));
		context.addTag(new RawTag("java"));
		context.addTag(new RawTag("knowledge"));
		
		FragmentTags result = this.object.newInstance(this.fragmentTags, context);
		
		assertEquals(0, result.toShow.size());
		
		assertEquals(3, result.toHide.size());
		assertEquals("java", result.toHide.get(0).getName());
		assertEquals("knowledge", result.toHide.get(1).getName());
		assertEquals("web", result.toHide.get(2).getName());
	}
}
