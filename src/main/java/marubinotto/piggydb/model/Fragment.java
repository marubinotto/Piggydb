package marubinotto.piggydb.model;

import java.util.List;

import marubinotto.util.Size;

import org.apache.commons.fileupload.FileItem;

public interface Fragment extends Classifiable, Password {
	
	public final static int TITLE_MAX_LENGTH = 100;
	
	public Fragment copyForUpdate();
	
	public String toStringWithDescendents();
	
	public String makeHeadline();
	
	public String makeHeadline(int maxLength);
	
	public String getTitle();
	
	public void setTitleByUser(String title, User user);
	
	public boolean canChangeTitle(User user);
	
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
	
	public boolean isPublic();
	
	public boolean isTrash();
	
	public boolean isUserFragment();
	
	public List<FragmentRelation> getParentRelations();
	
	public List<Fragment> getParents();
	
	public FragmentRelation getParentRelationByParentId(long parentId);
	
	public boolean hasChildren();
	
	public boolean hasChildren(boolean publicOnly);
	
	public List<FragmentRelation> getChildRelations();
	
	public List<FragmentRelation> getChildRelations(boolean publicOnly);
	
	public List<Fragment> getChildren();
}
