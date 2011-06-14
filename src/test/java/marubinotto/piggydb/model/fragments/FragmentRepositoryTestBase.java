package marubinotto.piggydb.model.fragments;

import java.util.List;

import marubinotto.piggydb.fixture.H2JdbcDaoFixtures;
import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.RepositoryTestBase;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.repository.FragmentRepositoryRI;

import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;

public abstract class FragmentRepositoryTestBase 
extends RepositoryTestBase<FragmentRepository> {

	protected FileRepository fileRepository;

	public FragmentRepositoryTestBase(
			RepositoryFactory<FragmentRepository> factory) {
		super(factory);
	}

	@Before
	public void given() throws Exception {
		super.given();
		this.fileRepository = new FileRepository.InMemory();
		this.object.setFileRepository(this.fileRepository);
	}
	
	@Parameters
	public static List<Object[]> factories() {
		return toParameters(
			// RI
			new RepositoryFactory<FragmentRepository>() {
				public FragmentRepository create() throws Exception {
					return new FragmentRepositoryRI();
				}
			},
			// H2 database
			new RepositoryFactory<FragmentRepository>() {
				public FragmentRepository create() throws Exception {
					return new H2JdbcDaoFixtures().createH2FragmentRepository();
				}
			}
		);
	}

	public Fragment newFragment() {
		return this.object.newInstance(getPlainUser());
	}
	
	public Fragment newFragmentWithTitle(String title) {
		Fragment fragment = newFragment();
		fragment.setTitleByUser(title, getPlainUser());
		return fragment;
	}
	
	public Fragment newFragmentWithTags(String ... tagNames)
	throws Exception {
		return newFragmentWithTitleAndTags(null, tagNames);
	}
	
	public Fragment newFragmentWithTitleAndTags(String title, String ... tagNames)
	throws Exception {
		Fragment fragment = newFragmentWithTitle(title);
		for (String tagName : tagNames) {
			fragment.addTagByUser(tagName, this.object.getTagRepository(), getPlainUser());
		}
		return fragment;
	}
	
	public Fragment newFragmentWithTitleAndContentAndTags(
		String title, 
		String content, 
		String ... tagNames)
	throws Exception {
		Fragment fragment = newFragmentWithTitleAndTags(title, tagNames);
		fragment.setContentByUser(content, getPlainUser());
		return fragment;
	}
	
	public Tag newTag(String name) {
		return this.object.getTagRepository().newInstance(name, getPlainUser());
	}
	
	public Tag newTagWithTags(String tagName, String ... parentTags) 
	throws Exception {
		Tag newTag = newTag(tagName);
		for (String parent : parentTags) {
			newTag.addTagByUser(parent, this.object.getTagRepository(), getPlainUser());
		}
		return newTag;
	}
	
	public Tag storedTag(String name) throws Exception {
		return this.object.getTagRepository().getByName(name);
	}
}
