package marubinotto.piggydb.model.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentList;
import marubinotto.piggydb.model.FragmentRelation;
import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.exception.AuthorizationException;
import marubinotto.piggydb.model.exception.InvalidTaggingException;
import marubinotto.piggydb.model.exception.InvalidTitleException;
import marubinotto.util.Assert;
import marubinotto.util.PasswordDigest;
import marubinotto.util.Size;
import marubinotto.util.message.CodedException;
import net.sf.click.util.ClickUtils;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;

public class RawFragment extends RawClassifiable implements Fragment {

  private String title;
  private String content;
  
  transient public FileItem fileInput;
  private String fileName;
  private String fileType;
  private Size fileSize;

  transient private List<FragmentRelation> parentRelations;
  transient private List<FragmentRelation> childRelations;
  
  private String password;
  
  private boolean asTag = false;
  private Long tagId;
  private Tag tag;
  
  public RawFragment() {
  }

  public RawFragment(User user) {
    super(user);
    ensureCanCreate(user);
  }
  
  public Fragment copyForUpdate() {
    return (Fragment)getDeepCopy();
  }
  
  @Override
  public String toString() {
    return "#" + getId() + (this.title != null ? " " + this.title : "");
  }

  
  //
  // Title
  //
  
  public String makeHeadline() {
    if (getTitle() != null) return getTitle();
    if (isFile()) return getFileName();
    return makeContentHeadline();
  }
  
  public String makeHeadline(int maxLength) {
    String headline = makeHeadline();
    if (headline == null) return null;

    if (headline.length() > maxLength) {
      headline = headline.substring(0, maxLength);
      headline = headline + "...";
    }
    return headline;
  }
  
  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    if (title != null && title.length() > TITLE_MAX_LENGTH) {
      throw new CodedException("fragment-title-invalid-max-size", String.valueOf(TITLE_MAX_LENGTH));
    }
    this.title = title;
  }

  public void setTitleByUser(String title, User user) {
    Assert.Arg.notNull(user, "user");
    
    if (ObjectUtils.equals(title, this.title) && !canChangeTitle(user)) return;
    
    ensureCanChangeTitle(user);
    
    setTitle(title);
    onPropertyChange(user);
  }
  
  
  //
  // Content
  //

  public String getContent() {
    return this.content;
  }
  
  public final static int HEADLINE_MAX_LENGTH = 100;
  
  public String makeContentHeadline() {
    String content = getContent();
    if (StringUtils.isBlank(content)) return null;

    String src = content.trim();
    String firstLine = getFirstLine(src).trim();
    String firstSentence = getFirstSentence(firstLine);

    String headline = firstSentence;

    // Trim to the max length
    if (headline.length() > HEADLINE_MAX_LENGTH) {
      headline = headline.substring(0, HEADLINE_MAX_LENGTH);
    }

    // trimmed to - the first line <or> the first sentence <or> the max length
    if (headline.length() < src.length()) {
      if (headline.equals(firstLine)) headline = headline + " ";
      headline = headline + "...";
    }

    return headline;
  }
  
  private static String getFirstLine(String text) {
    try {
      return new BufferedReader(new StringReader(text)).readLine();
    } 
    catch (IOException e) {
      throw new UnhandledException(e);
    }
  }
  
  private static String getFirstSentence(String text) {
    BreakIterator iterator = BreakIterator.getSentenceInstance();
    iterator.setText(text);
    int end = iterator.next();
    if (end == BreakIterator.DONE)
      return text;
    else
      return text.substring(0, end);
  }
  
  public boolean hasMoreThanHeadline() {
    if (isFile()) return true;
    if (StringUtils.isBlank(getContent())) return false;
    if (getTitle() != null) return true;
    
    // is text && has content && hasn't a title
    return !getContent().trim().equals(makeContentHeadline());
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setContentByUser(String content, User user) {
    Assert.Arg.notNull(user, "user");
    
    if (ObjectUtils.equals(content, this.content) && !canChange(user)) return;
    
    ensureCanChange(user);
    
    setContent(content);
    onPropertyChange(user);
  }
  
  public String toStringWithDescendents() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(toString());
    Stack<Long> stack = new Stack<Long>();
    toStringRecursively(this, buffer, stack);
    return buffer.toString();
  }
  
  private static void toStringRecursively(
      Fragment fragment, 
      StringBuffer buffer, 
      Stack<Long> stack) {
    if (!fragment.hasChildren()) return;
    
    stack.push(fragment.getId());
    buffer.append(" (");
    boolean first = true;
    for (Fragment child : fragment.getChildren()) {
      if (first) first = false; else buffer.append(", ");
      
      if (child.getId() == null) 
        throw new IllegalStateException("Missing fragment ID: " + buffer + "?");
      
      if (stack.contains(child.getId())) {
        buffer.append(child.toString() + " <loop>");
      }
      else {
        buffer.append(child.toString());
        toStringRecursively(child, buffer, stack);
      }
    }
    buffer.append(")");
    stack.pop();
  }
  
  
  //
  // Special
  //
  
  public boolean isHome() {
    return getId() != null ? getId().equals(Fragment.ID_HOME) : false;
  }

  
  //
  // Classification
  //

  public boolean isPublic() {
    return getClassification().isSubordinateOf(Tag.NAME_PUBLIC);
  }
  
  public boolean isTrash() {
    return getClassification().isSubordinateOf(Tag.NAME_TRASH);
  }
  
  public boolean isUserFragment() {
    return getClassification().isSubordinateOf(Tag.NAME_USER);
  }
  
  
  //
  // Authorization
  //
  
  public static boolean canCreate(User user) {
    Assert.Arg.notNull(user, "user");
    
    try { ensureCanCreate(user); return true; } 
    catch (AuthorizationException e) { return false; }
  }
  
  private static void ensureCanCreate(User user) {
    if (user.isViewer()) {
      throw new AuthorizationException("no-auth-to-create-fragment");
    }
  }
  
  public final boolean canChangeTitle(User user) {
    Assert.Arg.notNull(user, "user");
    
    try { ensureCanChangeTitle(user); return true; } 
    catch (AuthorizationException e) { return false; }
  }

  @Override
  public void ensureCanChange(User user) throws AuthorizationException {
    super.ensureCanChange(user);
    if (user.isViewer()) {
      throwNoAuthToChangeFragmentError();
    }
    if (isUserFragment()) {
      if (!user.isOwner() && !user.getName().equals(getTitle()))
        throwNoAuthToChangeFragmentError();
    }
  }
  
  protected void ensureCanChangeTitle(User user) throws AuthorizationException {
    ensureCanChange(user);
    if (isHome()) {
      throwNoAuthToChangeFragmentError();
    }
    if (isUserFragment()) {
      if (!user.isOwner()) throwNoAuthToChangeFragmentError();
    }
  }
  
  @Override
  protected void ensureCanAddTag(Tag tag, User user) throws AuthorizationException {
    super.ensureCanAddTag(tag, user);
    if (tag.isClassifiedAs(Tag.NAME_TRASH) && !canDelete(user)) {
      throwNoAuthToDeleteFragment();
    }
  }

  @Override
  public void ensureCanDelete(User user) throws AuthorizationException {
    super.ensureCanDelete(user);
    if (isHome()) {
      throwNoAuthToDeleteFragment();
    }
    if (isUserFragment()) {
      if (!user.isOwner()) throwNoAuthToDeleteFragment();
    }
  }
  
  private void throwNoAuthToChangeFragmentError() {
    throw new AuthorizationException(
      "no-auth-to-change-fragment", 
      ObjectUtils.toString(getId(), "null"));
  }
  
  private void throwNoAuthToDeleteFragment() {
    throw new AuthorizationException(
      "no-auth-to-delete-fragment", 
      ObjectUtils.toString(getId(), "null"));
  }

  
  //
  // File
  //
  
  public void setFileInput(FileItem fileInput) {
    Assert.Arg.notNull(fileInput, "fileInput");
    
    this.fileInput = fileInput;
    this.fileName = getFileName(fileInput);    
    this.fileType = getFileType(this.fileName);
    this.fileSize = new Size(fileInput.getSize());
  }
  
  public static String getFileType(String fileName) {
    String extension = FilenameUtils.getExtension(fileName);
    return StringUtils.isNotBlank(extension) ? extension.toLowerCase() : null;
  }

  public FileItem getFileInput() {
    return this.fileInput;
  }

  private static String getFileName(FileItem fileItem) {
    return FilenameUtils.getName(fileItem.getName());
  }

  public String getFileName() {
    return this.fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public boolean isFile() {
    return this.fileName != null;
  }

  public String getFileType() {
    return this.fileType;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public String getMimeType() {
    if (!isFile()) {
      return null;
    }
    
    // modification for the mime-type table
    if (this.fileName.endsWith(".svg"))
      return "image/svg+xml";
    
    return ClickUtils.getMimeType(this.fileName);
  }
  
  public boolean isImageFile() {
    if (!isFile()) {
      return false;
    }
    String mimeType = getMimeType();
    if (mimeType == null) {
      return false;
    }
    return mimeType.startsWith("image/");
  }
  
  public Size getFileSize() {
    return this.fileSize;
  }

  public void setFileSize(Size fileSize) {
    this.fileSize = fileSize;
  }
  
  
  //
  // Relations
  //
  
  public void checkTwoWayRelations() {
    for (FragmentRelation relation : getParentRelations()) relation.twoWay = false;
    for (FragmentRelation relation : getChildRelations()) relation.twoWay = false;
    
    for (FragmentRelation parent : getParentRelations()) {
      for (FragmentRelation child : getChildRelations()) {
        if (parent.isSamePairAs(child)) {
          parent.twoWay = true;
          child.twoWay = true;
        }
      }
    }
  }
  
  // Parents

  public List<FragmentRelation> getParentRelations() {
    if (this.parentRelations == null) {
      this.parentRelations = new ArrayList<FragmentRelation>();
    }
    return this.parentRelations;
  }
  
  public List<FragmentRelation> navigateToOneWayParents(Long contextRelationId) {
    List<FragmentRelation> relations = new ArrayList<FragmentRelation>();
    for (FragmentRelation relation : getParentRelations()) {
      // exclude the context relation
      if (contextRelationId != null && relation.getId().equals(contextRelationId))
        continue;
      
      // include only one-way parents
      if (!relation.twoWay) relations.add(relation);
    }
    return relations;
  }
  
  public List<Fragment> getParents() {
    List<Fragment> parents = new ArrayList<Fragment>();
    for (FragmentRelation parentRelation : getParentRelations()) 
      parents.add(parentRelation.from);
    return parents;
  }
  
  public void addParent(Fragment fragment) {
    Assert.Arg.notNull(fragment, "fragment");
    addParentRelation(new FragmentRelation(fragment, this));
  }

  public void addParentRelation(FragmentRelation relation) {
    Assert.Arg.notNull(relation, "relation");
    Assert.Arg.notNull(relation.from, "relation.from");
    if (relation.from.getId() != null)
      Assert.require(
        !relation.from.getId().equals(getId()), 
        "!relation.from.id.equals(this.id)");
    
    relation.to = this;
    getParentRelations().add(relation);
  }
  
  public void setParentRelations(List<FragmentRelation> relations) {
    Assert.Arg.notNull(relations, "relations");
    
    getParentRelations().clear();
    for (FragmentRelation parent : relations) {
      addParentRelation(parent);
    }
  }
  
  public FragmentRelation getParentRelationByParentId(long parentId) {
    for (FragmentRelation relation : getParentRelations()) {
      if (relation.from.getId().longValue() == parentId) {
        return relation;
      }
    }
    return null;
  }
  
  // Children

  public boolean hasChildren() {
    return getChildRelations().size() > 0;
  }
  
  public boolean hasChildren(boolean publicOnly) {
    return getChildRelations(publicOnly).size() > 0;
  }

  public List<FragmentRelation> getChildRelations() {
    if (this.childRelations == null) {
      this.childRelations = new ArrayList<FragmentRelation>();
    }
    return this.childRelations;
  }
  
  public List<FragmentRelation> getChildRelations(boolean publicOnly) {
    if (publicOnly) {
      List<FragmentRelation> publicChildren = new ArrayList<FragmentRelation>();
      for (FragmentRelation relation : getChildRelations()) {
        if (relation.to.isPublic()) publicChildren.add(relation);
      }
      return publicChildren;
    }
    else {
      return getChildRelations();
    }
  }
  
  public boolean isNavigableToChildren(FragmentRelation contextRelation) {
    return navigateToChildren(contextRelation).size() > 0;
  }
  
  public List<FragmentRelation> navigateToChildren(FragmentRelation contextRelation) {
    if (contextRelation == null) return getChildRelations();
    return navigateToChildren(contextRelation.from.getId());
  }
  
  public List<FragmentRelation> navigateToChildren(Long contextParentId) {
    if (contextParentId == null) return getChildRelations();
    
    List<FragmentRelation> relations = new ArrayList<FragmentRelation>();
    for (FragmentRelation relation : getChildRelations()) {
      if (!contextParentId.equals(relation.to.getId())) 
        relations.add(relation);
    }
    return relations;
  }
  
  public List<Fragment> getChildren() {
    List<Fragment> children = new ArrayList<Fragment>();
    for (FragmentRelation childRelation : getChildRelations()) 
      children.add(childRelation.to);
    return children;
  }
  
  public FragmentList<RawFragment> getRawChildren() {
    return new FragmentList<RawFragment>(this).getChildren();
  }
  
  public void addChild(Fragment fragment) {
    Assert.Arg.notNull(fragment, "fragment");
    addChildRelation(new FragmentRelation(this, fragment));
  }
  
  public void addChildRelation(FragmentRelation relation) {
    Assert.Arg.notNull(relation, "relation");
    Assert.Arg.notNull(relation.to, "relation.to");
    if (relation.to.getId() != null) 
      Assert.require(!relation.to.getId().equals(getId()), "!relation.to.id.equals(this.id)");
    
    relation.from = this;
    getChildRelations().add(relation);
  }
  
  public void setChildRelations(List<FragmentRelation> childRelations) {
    Assert.Arg.notNull(childRelations, "childRelations");
    
    getChildRelations().clear();
    for (FragmentRelation child : childRelations) {
      addChildRelation(child);
    }
  }

  public void sortChildRelations() {
    if (this.childRelations == null) {
      return;
    }
    Collections.sort(this.childRelations, childRelationsComparator);
  }
  
  @SuppressWarnings("rawtypes")
  private static final Comparator nullLowComparator = new NullComparator(false);
  
  public static final Comparator<FragmentRelation> childRelationsComparator = 
    new Comparator<FragmentRelation>() {
      @SuppressWarnings("unchecked")
      public int compare(FragmentRelation o1, FragmentRelation o2) {
        int result = nullLowComparator.compare(o2.priority, o1.priority);
        if (result != 0) return result;
        return nullLowComparator.compare(o1.getId(), o2.getId());
      }
    };
  
  
  //
  // Password
  //
  
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
  
  public boolean validatePassword(String password) throws Exception {
    Assert.Property.requireNotNull(title, "title");
    
    // the default password is the same as the fragment title (user name)
    if (this.password == null) return this.title.equals(password);
    
    PasswordDigest pd = new PasswordDigest();
    String encrypted = pd.digestWithStoredSalt(password, this.password);
    return encrypted.equals(this.password);
  }

  public void changePassword(String password) throws Exception {
    Assert.Arg.notNull(password, "password");
    
    PasswordDigest pd = new PasswordDigest();
    this.password = pd.createSshaDigest(password);
  }
  
  
  //
  // As a tag
  //
  
  public boolean isTag() {
    return this.asTag;
  }
  
  private void setAsTag(boolean asTag) {
    this.asTag = asTag;
  }
  
  public Long getTagId() {
    return this.tagId;
  }

  public void setTagId(Long tagId) {
    this.tagId = tagId;
    setAsTag(this.tagId != null);
  }
  
  public Tag asTag() {
    return this.tag;
  }
  
  public void setTag(Tag tag) {
    this.tag = tag;
  }
  
  public void setAsTagByUser(boolean asTag, User user) {
    Assert.Arg.notNull(user, "user");
    
    if (ObjectUtils.equals(asTag, this.asTag) && !canChange(user)) return;
    
    ensureCanChange(user);
    
    setAsTag(asTag);
    onPropertyChange(user);
  }

  // ensure: asTag() != null if isTag() == true
  public void validateAsTag(User user, TagRepository tagRepository) 
  throws Exception {
    Tag tag = asTag();
    if (tag == null && getTagId() != null) {
      tag = tagRepository.get(getTagId());
      setTag(tag);
    }
    
    // new or update
    if (isTag()) {
      if (StringUtils.isBlank(getTitle()))
        throw new InvalidTitleException("blank-tag-fragment-title");

      if (tag == null) {  // new
        tag = tagRepository.newInstance(getTitle(), user);
        setTag(tag);
      }
      
      tag.setNameByUser(getTitle(), user);
      tag.updateTagsByUser(getClassification().getTagNames(), tagRepository, user);
      tagRepository.validate(tag);
    }
    // delete
    else {
      if (tag != null) tag.ensureCanDelete(user);
    }
  }
  
  public void syncWith(Tag tag, User user) throws InvalidTaggingException {
    Assert.Arg.notNull(tag, "tag");
    Assert.Arg.notNull(user, "user");
    
    setTitleByUser(tag.getName(), user);
    syncClassificationWith(tag);
  }
}
