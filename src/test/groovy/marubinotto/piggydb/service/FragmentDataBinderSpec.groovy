package marubinotto.piggydb.service

import marubinotto.piggydb.DataAccessSpec
import marubinotto.piggydb.model.auth.User
import marubinotto.piggydb.model.entity.RawFragment
import marubinotto.util.message.MapMessageSource
import marubinotto.util.message.MessageSource

class FragmentDataBinderSpec extends DataAccessSpec {

  FragmentDataBinder object = new FragmentDataBinder()
  
  RawFragment fragment = new RawFragment()
  MessageSource messageSource = new MapMessageSource([:])
  
  def "bind an asTag flag"() {
    when:
      this.object.asTag = null
      this.object.bindValues(
        this.fragment, this.owner, this.messageSource, this.database.tagRepository)
    then:
      this.fragment.isTag() == false
  }
}
