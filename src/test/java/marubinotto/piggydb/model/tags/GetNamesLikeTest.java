package marubinotto.piggydb.model.tags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import marubinotto.piggydb.model.TagRepository;

import org.junit.Before;
import org.junit.Test;

public class GetNamesLikeTest extends TagRepositoryTestBase {
	
	public GetNamesLikeTest(RepositoryFactory<TagRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		
		this.object.register(newTag("java"));
		this.object.register(newTag("jruby"));
		this.object.register(newTag("tech"));
		this.object.register(newTag("OO"));
		this.object.register(newTag("ocaml"));
	}
	
	@Test
	public void returnTwo() throws Exception {
		// When
		List<String> results = this.object.getNamesLike("j");
		
		// Then
		assertEquals(2, results.size());
		assertTrue(results.contains("java"));
		assertTrue(results.contains("jruby"));
	}
	
	@Test
	public void returnOne() throws Exception {
		// When
		List<String> results = this.object.getNamesLike("t");
		
		// Then
		assertEquals(1, results.size());
		assertTrue(results.contains("tech"));
	}
	
	@Test
	public void ignoreCase_withLower() throws Exception {
		// When
		List<String> results = this.object.getNamesLike("o");
		
		// Then
		assertEquals(2, results.size());
		assertTrue(results.contains("ocaml"));
		assertTrue(results.contains("OO"));
	}
	
	@Test
	public void ignoreCase_withUpper() throws Exception {
		// When
		List<String> results = this.object.getNamesLike("O");
		
		// Then
		assertEquals(2, results.size());
		assertTrue(results.contains("ocaml"));
		assertTrue(results.contains("OO"));
	}
	
	@Test
	public void returnZero() throws Exception {
		// When
		List<String> results = this.object.getNamesLike("a");
		
		// Then
		assertEquals(0, results.size());
	}
}
