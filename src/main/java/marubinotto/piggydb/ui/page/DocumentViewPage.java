package marubinotto.piggydb.ui.page;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.common.AbstractTemplateWebResource;
import marubinotto.piggydb.ui.page.common.PageImports;
import marubinotto.piggydb.ui.wiki.WikiParser;
import marubinotto.util.Assert;

public class DocumentViewPage extends AbstractTemplateWebResource {

  @Override
  protected boolean needsAuthentication() {
    return false;
  }

  //
  // Input
  //

  public Long id;
  public String name;
  
  public Fragment fragment;

  @Override
  protected boolean onPreInit() throws Exception {
    if (this.id == null && this.name == null) {
      this.id = Fragment.ID_HOME;
    }
    
    FragmentRepository repository = getDomain().getFragmentRepository();
    if (this.id != null) {
      this.fragment = repository.get(this.id.longValue());
    }
    else if (this.name != null) {
      Tag tag = getDomain().getTagRepository().getByName(this.name);
      if (tag != null) this.fragment = repository.asFragment(tag);
    }
    
    if (this.fragment == null) {
      getLogger().info("Missing fragment: id:" + this.id + " name:" + this.name);
      return true;
    }

    if (!isAuthenticated() && !this.fragment.isPublic()) {
      getLogger().info("Forbidden: #" + this.id);
      setRedirectToLogin();
      return false;
    }

    fetchTagsAdditionally(this.fragment);

    return true;
  }

  private void fetchTagsAdditionally(Fragment fragment) throws Exception {
    List<Fragment> tagNotFetched = new ArrayList<Fragment>();

    // Grandchildren
    tagNotFetched.addAll(ModelUtils.collectChildrenOfEach(fragment.getChildren()));
    // Great-grandchildren
    tagNotFetched.addAll(ModelUtils.collectChildrenOfEach(tagNotFetched));

    getDomain().getFragmentRepository().refreshClassifications(tagNotFetched);
  }

  //
  // Model
  //

  public Boolean publicOnly;
  public String additionalCssImports;
  public String htmlTitle;
  public String databaseTitle;
  public List<Fragment> parents;

  @Override
  protected void setModels() throws Exception {
    super.setModels();
    
    this.publicOnly = !isAuthenticated();
    this.additionalCssImports = PageImports.additionalCssImports.toString();
    this.wikiParser = (WikiParser)getBean("documentViewWikiParser");
    
    if (this.fragment != null) {
      // titles
      if (this.fragment.isHome()) {
        this.databaseTitle = getDomain().getGlobalSetting().getDatabaseTitle();
        this.htmlTitle = this.databaseTitle;
      }
      else {
        this.htmlTitle = this.fragment.getTitle();
      }
      // parents
      if (this.publicOnly)
        this.parents = getPublicParents(this.fragment, getDomain().getFragmentRepository());
      else
        this.parents = this.fragment.getParents();
    }
  }
  
  public static List<Fragment> getPublicParents(Fragment fragment, FragmentRepository repository) 
  throws Exception {
    Assert.Arg.notNull(fragment, "fragment");
    Assert.Arg.notNull(repository, "repository");
    
    List<Fragment> parents = fragment.getParents();
    repository.refreshClassifications(parents);
    
    List<Fragment> publicParents = new ArrayList<Fragment>();
    for (Fragment parent : parents) {
      if (parent.isPublic()) publicParents.add(parent);
    }
    return publicParents;
  }
}
