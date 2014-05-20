package marubinotto.piggydb.service;

import static java.lang.Long.parseLong;
import static marubinotto.util.message.CodedException.getCodedMessageOrThrow;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.entity.RawEntity;
import marubinotto.piggydb.model.exception.DuplicateException;
import marubinotto.piggydb.model.exception.InvalidTagNameException;
import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.piggydb.model.exception.InvalidTitleException;
import marubinotto.util.Assert;
import marubinotto.util.message.MessageSource;
import marubinotto.util.time.DateTime;

public class FragmentDataBinder {

  public String asTag;
  public String title;
  public String tags;
  public String content;
  public String timestamp;

  public String error;
  public Map<String, String> fieldErrors = new HashMap<String, String>();
  
  public boolean hasErrors() {
    if (this.error != null) return true;
    if (!this.fieldErrors.isEmpty()) return true;
    return false;
  }

  protected boolean asTag() {
    return this.asTag != null;
  }
  
  protected DateTime getOriginalTimestamp() {
    if (isBlank(this.timestamp)) return null;
    return new DateTime(parseLong(this.timestamp));
  }
  
  protected static String emptyToNull(String value) {
    if (value == null) return null;
    return value.equals("") ? null : value;
  }
  
  public void bindValues(
    Fragment fragment, 
    User user, 
    MessageSource source, 
    TagRepository tagRepository) 
  throws Exception {
    Assert.Arg.notNull(fragment, "fragment");
    Assert.Arg.notNull(user, "user");
    Assert.Arg.notNull(source, "source");
    Assert.Arg.notNull(tagRepository, "tagRepository");
    
    // as a tag
    try {
      fragment.setAsTagByUser(asTag(), user);
    }
    catch (Exception e) {
      this.fieldErrors.put("asTag", getCodedMessageOrThrow(e, source));
    }
    
    // title
    try {
      fragment.setTitleByUser(trimToNull(this.title), user);
    } 
    catch (Exception e) {
      this.fieldErrors.put("title", getCodedMessageOrThrow(e, source));
    }
    
    // tags
    List<String> tagNames = FragmentFormUtils.splitTagsString(this.tags);
    try {
      fragment.updateTagsByUser(tagNames, tagRepository, user);
    } 
    catch (Exception e) {
      this.fieldErrors.put("tags", getCodedMessageOrThrow(e, source));
    }
    
    // content
    try {
      fragment.setContentByUser(emptyToNull(this.content), user);
    } 
    catch (Exception e) {
      this.fieldErrors.put("content", getCodedMessageOrThrow(e, source));
    }
    
    if (hasErrors()) return;
    
    try {
      fragment.validateAsTag(user, tagRepository);
    }
    catch (Exception e) {
      if (e instanceof InvalidTitleException ||
          e instanceof InvalidTagNameException ||
          e instanceof DuplicateException) {
        this.fieldErrors.put("title", getCodedMessageOrThrow(e, source));
      }
      else if (e instanceof InvalidTaggingException) {
        this.fieldErrors.put("tags", getCodedMessageOrThrow(e, source));
      }
      else {
        this.error = getCodedMessageOrThrow(e, source);
      }
    }
    
    // to enable optimistic lock
    DateTime originalTimestamp = getOriginalTimestamp();
    if (originalTimestamp != null && fragment instanceof RawEntity) {
      ((RawEntity)fragment).setUpdateDatetime(originalTimestamp);
    }
  } 
}
