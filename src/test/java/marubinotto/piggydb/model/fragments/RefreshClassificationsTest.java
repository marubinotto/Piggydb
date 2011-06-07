package marubinotto.piggydb.model.fragments;

import static marubinotto.piggydb.model.Assert.assertClassificationEquals;
import static marubinotto.util.CollectionUtils.list;
import static marubinotto.util.CollectionUtils.map;

import java.util.HashMap;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;

import org.junit.Before;
import org.junit.Test;

public class RefreshClassificationsTest extends FragmentRepositoryTestBase {
	
	private Fragment fragment1;
	private Fragment fragment2;
	
	public RefreshClassificationsTest(RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}
	
	@Before
	public void given() throws Exception {
		super.given();

		this.fragment1 = newFragmentWithTitleAndTags("Adaptive Design", "agile");
		this.object.register(this.fragment1);
		
		this.fragment2 = newFragmentWithTitleAndTags("The New Methodology", "agile");
		this.object.register(this.fragment2);
	}

	@Test
	public void tagAdded() throws Exception {
		// Given
		Fragment fragment = this.object.get(this.fragment1.getId());
		fragment.addTagByUser("software", this.object.getTagRepository(), getPlainUser());
		this.object.update(fragment);
		
		// When
		this.object.refreshClassifications(list(this.fragment1));
		
		// Then
		assertClassificationEquals(
			map("agile", null).map("software", null), 
			this.fragment1.getClassification());
	}
	
	@Test
	public void grandparentTagAdded() throws Exception {
		// Given
		TagRepository tagRepository = this.object.getTagRepository();
		Tag tag = tagRepository.getByName("agile");
		tag.addTagByUser("methodology", tagRepository, getPlainUser());
		tagRepository.update(tag);
		
		// When
		this.object.refreshClassifications(list(this.fragment1));
		
		// Then
		assertClassificationEquals(
			map("agile", map("methodology", null)), 
			this.fragment1.getClassification());
	}
	
	@Test
	public void grandparentTagAdded_refreshTwo() throws Exception {
		// Given
		TagRepository tagRepository = this.object.getTagRepository();
		Tag tag = tagRepository.getByName("agile");
		tag.addTagByUser("methodology", tagRepository, getPlainUser());
		tagRepository.update(tag);
		
		// When
		this.object.refreshClassifications(list(this.fragment1, this.fragment2));
		
		// Then
		assertClassificationEquals(
			map("agile", map("methodology", null)), 
			this.fragment1.getClassification());
		assertClassificationEquals(
			map("agile", map("methodology", null)), 
			this.fragment2.getClassification());
	}
	
	@Test
	@SuppressWarnings("rawtypes")
	public void tagRemoved() throws Exception {
		// Given
		Fragment fragment = this.object.get(this.fragment1.getId());
		fragment.removeTagByUser("agile", getPlainUser());
		this.object.update(fragment);
		
		// When
		this.object.refreshClassifications(list(this.fragment1));
		
		// Then
		assertClassificationEquals(
			new HashMap(), 
			this.fragment1.getClassification());
	}
	
	@Test
	public void refreshTwoWithSameIds() throws Exception {
		// Given
		Fragment fragment = this.object.get(this.fragment1.getId());
		fragment.addTagByUser("software", this.object.getTagRepository(), getPlainUser());
		this.object.update(fragment);
		
		// When
		Fragment f1 = this.fragment1.copyForUpdate();
		Fragment f2 = this.fragment1.copyForUpdate();
		this.object.refreshClassifications(list(f1, f2));
		
		// Then
		assertClassificationEquals(
			map("agile", null).map("software", null), f1.getClassification());
		assertClassificationEquals(
			map("agile", null).map("software", null), f2.getClassification());
	}
}
