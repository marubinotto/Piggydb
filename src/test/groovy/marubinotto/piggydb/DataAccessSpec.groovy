package marubinotto.piggydb

import spock.lang.Specification;
import marubinotto.piggydb.impl.InMemoryDatabase
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.enums.Role
import marubinotto.piggydb.model.FragmentRepository
import marubinotto.piggydb.model.TagRepository

abstract class DataAccessSpec extends Specification {

  InMemoryDatabase database = new InMemoryDatabase()
  
  FragmentRepository fragmentRepository = this.database.getFragmentRepository()
  TagRepository tagRepository = this.fragmentRepository.getTagRepository()
  
  User owner = new User("owner")
  
  def setup() {
    this.owner.addRole(Role.OWNER)
    assert this.owner.isOwner()
  }
}
