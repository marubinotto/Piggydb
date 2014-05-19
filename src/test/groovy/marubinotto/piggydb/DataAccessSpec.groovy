package marubinotto.piggydb

import spock.lang.Specification;
import marubinotto.piggydb.impl.InMemoryDatabase
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.enums.Role

abstract class DataAccessSpec extends Specification {

  InMemoryDatabase database = new InMemoryDatabase()
  User owner = new User("owner")
  
  def setup() {
    this.owner.addRole(Role.OWNER)
    assert this.owner.isOwner()
  }
}
