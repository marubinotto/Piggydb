package marubinotto.piggydb.model;

import java.util.List;

import marubinotto.util.Size;

import org.apache.commons.fileupload.FileItem;

public interface Fragment extends Classifiable, Password {
	
	public final static int TITLE_MAX_LENGTH = 100;
	
	public Fragment copyForUpdate();
	
	public String toStringWithDescendents();
	
	
	//
	// Title
	//
	
	public String makeHeadline();
	
	public String makeHeadline(int maxLength);
	
	public boolean hasMoreThanHeadline();
	
	public String getTitle();
	
	public void setTitleByUser(String title, User user);
	
	public boolean canChangeTitle(User user);
	
	
	//
	// Content
	//
	
	public String getContent();
	
	public String makeContentHeadline();
	
	public void setContentByUser(String content, User user);
	
	public void setFileInput(FileItem fileInput);
	
	public FileItem getFileInput();
	
	public boolean isFile();
	
	public String getFileName();
	
	public String getFileType();
	
	public String getMimeType();
	
	public boolean isImageFile();
	
	public Size getFileSize();
	
	
	//
	// Tagged
	//
	
	public boolean isPublic();
	
	public boolean isTrash();
	
	public boolean isUserFragment();
	
	
	//
	// Relationships
	//
	
	public List<FragmentRelation> getParentRelations();
	
	public List<Fragment> getParents();
	
	public FragmentRelation getParentRelationByParentId(long parentId);
	
	public boolean hasChildren();
	
	public boolean hasChildren(boolean publicOnly);
	
	public List<FragmentRelation> getChildRelations();
	
	public List<FragmentRelation> getChildRelations(boolean publicOnly);
	
	public List<Fragment> getChildren();
	
	
	//
	// As a tag
	//
	
	public boolean isTag();
	
	public void setAsTagByUser(boolean asTag, User user);
	
	public Long getTagId();
	
	public Tag asTag();
	
	
	//
	// Validation involving multiple properties
	//
	
	public void validate(User user, TagRepository tagRepository);
}
