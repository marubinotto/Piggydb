package marubinotto.piggydb.service

import marubinotto.piggydb.DataAccessSpec
import marubinotto.piggydb.model.auth.User
import marubinotto.piggydb.model.entity.RawFragment
import marubinotto.util.message.MessageSource
import marubinotto.util.message.MockMessageSource

class FragmentDataBinderSpec extends DataAccessSpec {

  FragmentDataBinder object = new FragmentDataBinder()
  
  RawFragment fragment = new RawFragment()
  MessageSource messageSource = new MockMessageSource()
  
  def "bind an asTag flag"() {
    when:
      this.object.asTag = null
      doBind()
    then:
      this.fragment.isTag() == false
      
    when:
      this.object.asTag = "on"
      doBind()
    then:
      this.fragment.isTag() == true
  }
  
  def "bind a title"() {
    when:
      this.object.title = "hello"
      doBind()
    then:
      this.fragment.getTitle() == "hello"
      
    when:
      this.object.title = (1..151).sum { 'a' }
      doBind()
    then:
      this.object.hasErrors() == true
      this.object.fieldErrors == [title: 'fragment-title-invalid-max-size {150}']
  }
  
  def "bind a content"() {
    when:
      this.object.content = "Knowledge is power."
      doBind()
    then:
      this.fragment.getContent() == "Knowledge is power."
  }
  
  private def doBind() {
    this.object.bindValues(
      this.fragment, this.owner, this.messageSource, this.database.tagRepository)
  }
}
