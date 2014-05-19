package marubinotto.piggydb

import spock.lang.Specification;
import marubinotto.piggydb.impl.InMemoryDatabase

abstract class DataAccessSpec extends Specification {

  InMemoryDatabase database = new InMemoryDatabase()
}
