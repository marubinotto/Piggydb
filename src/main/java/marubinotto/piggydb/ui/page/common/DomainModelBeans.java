package marubinotto.piggydb.ui.page.common;

import marubinotto.piggydb.model.Authentication;
import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.FilterRepository;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.GlobalSetting;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.util.Assert;
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
}
