package marubinotto.piggydb.ui.page;

import java.util.ArrayList;
import java.util.List;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.ModelUtils;
import marubinotto.piggydb.ui.page.common.AbstractTemplateWebResource;
import marubinotto.piggydb.ui.page.common.PageImports;
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
  public Fragment fragment;

  @Override
  protected boolean onPreInit() throws Exception {
    if (this.id == null) this.id = Fragment.ID_HOME;
    
    this.fragment = getDomain().getFragmentRepository().get(this.id.longValue());
    if (this.fragment == null) {
      getLogger().info("Missing fragment: #" + this.id);
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
  public String databaseTitle;
  public List<Fragment> parents;

  @Override
  protected void setModels() throws Exception {
    super.setModels();
    
    this.publicOnly = !isAuthenticated();
    this.additionalCssImports = PageImports.additionalCssImports.toString();
    
    if (this.fragment != null) {
      if (this.fragment.isHome()) {
        this.databaseTitle = getDomain().getGlobalSetting().getDatabaseTitle();
      }
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
