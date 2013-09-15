package marubinotto.piggydb.model.tags;

import static junit.framework.Assert.assertEquals;
import static marubinotto.util.CollectionUtils.list;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;

public class GetByIdsTest extends TagRepositoryTestBase {

  public GetByIdsTest(
      RepositoryFactory<TagRepository> factory) {
    super(factory);
  }
  
  private Long id1, id2, id3;
  
  @Before
  public void given() throws Exception {
    super.given();
    
    this.id1 = this.object.register(newTag("aaa"));
    this.id2 = this.object.register(newTag("bbb"));
    this.id3 = this.object.register(newTagFragment("ccc", 1L));
  }
  
  @Test
  public void noHit() throws Exception {
    List<Tag> result = this.object.getByIds(list(10L));
    
    assertEquals(0, result.size());
  }
  
  @Test
  public void oneId() throws Exception {
    List<Tag> result = this.object.getByIds(list(this.id1));
    
    assertEquals(1, result.size());
    assertEquals("aaa", result.get(0).getName());
    assertEquals(false, result.get(0).isTagFragment());
  }
  
  @Test
  public void twoIds() throws Exception {
    List<Tag> result = this.object.getByIds(list(this.id1, this.id2));
    
    assertEquals(2, result.size());
    assertEquals("aaa", result.get(0).getName());
    assertEquals(false, result.get(0).isTagFragment());
    assertEquals("bbb", result.get(1).getName());
    assertEquals(false, result.get(1).isTagFragment());
  }
  
  @Test
  public void threeIds() throws Exception {
    List<Tag> result = this.object.getByIds(list(this.id1, this.id2, this.id3));
    
    assertEquals(3, result.size());
    assertEquals("aaa", result.get(0).getName());
    assertEquals(false, result.get(0).isTagFragment());
    assertEquals("bbb", result.get(1).getName());
    assertEquals(false, result.get(1).isTagFragment());
    assertEquals("ccc", result.get(2).getName());
    assertEquals(true, result.get(2).isTagFragment());
  }
}
