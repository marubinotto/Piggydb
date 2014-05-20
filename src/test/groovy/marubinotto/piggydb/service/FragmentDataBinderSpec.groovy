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
  
  def "has no errors by default"() {
    expect:
      this.object.hasErrors() == false
  }
  
  def "binds an asTag flag"() {
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
  
  def "binds a title"() {
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
  
  def "binds a content"() {
    when:
      this.object.content = "Knowledge is power."
      doBind()
    then:
      this.fragment.getContent() == "Knowledge is power."
  }
  
  def "binds new tags"() {
    when:
      this.object.tags = "foo, bar"
      doBind()
    then:
      getBoundTags() == ["null:foo", "null:bar"] as Set
  }
  
  def "binds a new tag and an existing tag"() {
    given:
      Long id = registerTag("foo")
    when:
      this.object.tags = "foo, bar"
      doBind()
    then:
      getBoundTags() == ["${id}:foo", "null:bar"] as Set
  }
  
  private def doBind() {
    this.object.bindValues(
      this.fragment, this.owner, this.messageSource, this.tagRepository)
  }
  
  private Set<String> getBoundTags() {
    this.fragment.getClassification().getTags().collect {
      "${it.getId()}:${it.getName()}"
    }.toSet()
  }
}
