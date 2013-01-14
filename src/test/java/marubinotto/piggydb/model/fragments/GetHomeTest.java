package marubinotto.piggydb.model.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;

import org.junit.Test;

public class GetHomeTest extends FragmentRepositoryTestBase {

  public GetHomeTest(RepositoryFactory<FragmentRepository> factory) {
    super(factory);
  }
  
  @Test
  public void homeFragmentShouldBeCreatedIfNotExist() throws Exception {
    assertEquals(0, this.object.size());
    
    Fragment home = this.object.getHome(false, getOwner());
    assertEquals(0, home.getId().longValue());
    
    assertEquals(1, this.object.size());
  }
  
  @Test
  public void viewerCanNotCreateHomeFragment() throws Exception {
    Fragment home = this.object.getHome(false, getViewer());
    assertNull(home);
  }
}
