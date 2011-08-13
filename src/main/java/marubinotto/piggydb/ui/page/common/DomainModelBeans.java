package marubinotto.piggydb.ui.page.common;

import java.util.List;

import marubinotto.piggydb.model.Authentication;
import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.FilterRepository;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.GlobalSetting;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.util.Assert;
import marubinotto.util.procedure.Procedure;
import marubinotto.util.procedure.Transaction;

import org.springframework.beans.factory.BeanFactory;

public class DomainModelBeans {

	private BeanFactory factory;

	public DomainModelBeans(BeanFactory factory) {
		Assert.Arg.notNull(factory, "factory");
		this.factory = factory;
	}
	
	public Transaction getTransaction() {
  	return (Transaction)this.factory.getBean("transaction");
  }

	public GlobalSetting getGlobalSetting() {
		return (GlobalSetting)this.factory.getBean("globalSetting");
	}

	public Authentication getAuthentication() {
		return (Authentication)this.factory.getBean("authentication");
	}
  
	public TagRepository getTagRepository() {
  	return (TagRepository)this.factory.getBean("tagRepository");
  }
  
	public FragmentRepository getFragmentRepository() {
  	return (FragmentRepository)this.factory.getBean("fragmentRepository");
  }
  
	public FilterRepository getFilterRepository() {
  	return (FilterRepository)this.factory.getBean("filterRepository");
  }
  
	public FileRepository getFileRepository() {
  	return (FileRepository)this.factory.getBean("fileRepository");
  }
	
	public void saveFragment(final Fragment fragment, User user) throws Exception {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
		Assert.Arg.notNull(user, "user");
		
		fragment.validateTagRole(user, getTagRepository());

		getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {
				getFragmentRepository().update(fragment);
				return null;
			}
		});
	}
	
	public void tagToFragments(
		final List<Fragment> fragments, 
		final Tag tag, 
		final User user)
	throws Exception {
		Assert.Arg.notNull(fragments, "fragments");
		Assert.Arg.notNull(tag, "tag");
		Assert.Arg.notNull(user, "user");
		
		getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {
				for (Fragment fragment : fragments) {
					fragment.addTagByUser(tag, user);
					fragment.validateTagRole(user, getTagRepository());
					getFragmentRepository().update(fragment);
				}
				return null;
			}
		});
	}
	
	public void removeTagFromFragments(
		final List<Fragment> fragments, 
		final String tagName,
		final User user)
	throws Exception {
		Assert.Arg.notNull(fragments, "fragments");
		Assert.Arg.notNull(tagName, "tagName");
		Assert.Arg.notNull(user, "user");
		
		getTransaction().execute(new Procedure() {
			public Object execute(Object input) throws Exception {
				for (Fragment fragment : fragments) {
					fragment.removeTagByUser(tagName, user);
					fragment.validateTagRole(user, getTagRepository());
					getFragmentRepository().update(fragment);
				}
				return null;
			}
		});
	}
}
