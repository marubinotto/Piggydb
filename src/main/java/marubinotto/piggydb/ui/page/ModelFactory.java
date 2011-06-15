package marubinotto.piggydb.ui.page;

import java.util.Map;

import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.FilterRepository;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.predicate.Preformatted;
import marubinotto.piggydb.ui.page.model.FragmentTags;
import marubinotto.piggydb.ui.page.model.SelectedFragments;
import marubinotto.piggydb.ui.wiki.WikiParser;
import marubinotto.util.procedure.Transaction;

import org.apache.velocity.app.FieldMethodizer;

public abstract class ModelFactory extends AbstractPage {
	
	public ModelFactory() {	
	}
	
	
	//
	// Domain models
	//

    private WikiParser getWikiParser() {
    	return (WikiParser)getBean("wikiParser");
    }

    protected Transaction getTransaction() {
    	return (Transaction)getBean("transaction");
    }
    
    protected TagRepository getTagRepository() {
    	return (TagRepository)getBean("tagRepository");
    }
    
    protected FragmentRepository getFragmentRepository() {
    	return (FragmentRepository)getBean("fragmentRepository");
    }
    
    protected FilterRepository getFilterRepository() {
    	return (FilterRepository)getBean("filterRepository");
    }
    
    protected FileRepository getFileRepository() {
    	return (FileRepository)getBean("fileRepository");
    }
	
	
	//
	// Model for templates
	//
	
	public static final String SK_SELECTED_FRAGMENTS = "selectedFragments";
	protected static final int ALMOST_UNLIMITED_PAGE_SIZE = 1000000;
	
	private static final FieldMethodizer CONSTANTS_TAG = new FieldMethodizer(Tag.class.getName());

	public FieldMethodizer tagConstants = CONSTANTS_TAG;
	public WikiParser wikiParser;
	public Preformatted preformatted = Preformatted.INSTANCE;
	public FragmentTags fragmentTagsPrototype = new FragmentTags();
	public Map<Long, String> selectedFragments;
	
	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		this.wikiParser = getWikiParser();	
	}

	protected SelectedFragments getSelectedFragments() {
		return createOrGetObjectInSession(
			SK_SELECTED_FRAGMENTS, 
			new Factory<SelectedFragments>() {
				public SelectedFragments create() {
					return new SelectedFragments();
				}
			});
	}
	
	protected void setSelectedFragments() throws Exception {
		SelectedFragments fragments = getSelectedFragments();
		if (!fragments.isEmpty()) {
			this.selectedFragments = fragments.getTitles(getFragmentRepository());
		}
	}
}
