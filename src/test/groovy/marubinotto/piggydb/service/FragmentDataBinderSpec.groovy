package marubinotto.piggydb.service

import marubinotto.piggydb.DataAccessSpec
import marubinotto.piggydb.model.auth.User
import marubinotto.piggydb.model.entity.RawFragment
import marubinotto.piggydb.model.enums.Role
import marubinotto.util.message.MapMessageSource
import marubinotto.util.message.MessageSource

class FragmentDataBinderSpec extends DataAccessSpec {

  FragmentDataBinder object = new FragmentDataBinder()
  
  RawFragment fragment = new RawFragment()
  MessageSource messageSource = new MapMessageSource([:])
  User owner = new User("owner")
  
  def setup() {
    this.owner.addRole(Role.OWNER)
  }
  
  def "bind an asTag flag"() {
    when:
      this.object.asTag = null
      this.object.bindValues(
        this.fragment, this.owner, this.messageSource, this.database.tagRepository)
    then:
      this.fragment.isTag() == false
  }
}
