package marubinotto.piggydb.model.fragments;

import static org.junit.Assert.assertEquals;
import marubinotto.piggydb.model.FragmentRepository;

import org.junit.Test;

public class GetFragmentsAtHomeTest extends FragmentRepositoryTestBase {

  public GetFragmentsAtHomeTest(RepositoryFactory<FragmentRepository> factory) {
    super(factory);
  }
  
  @Test
  public void ownerShouldGetEmptyWhenNoHome() throws Exception {
    assertEquals(0, this.object.getFragmentsAtHome(getOwner()).size());
  }
  
  @Test
  public void plainUserShouldGetEmptyWhenNoHome() throws Exception {
    assertEquals(0, this.object.getFragmentsAtHome(getPlainUser()).size());
  }
  
  @Test
  public void viewerShouldGetEmptyWhenNoHome() throws Exception {
    assertEquals(0, this.object.getFragmentsAtHome(getViewer()).size());
  }
}
