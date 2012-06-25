package marubinotto.piggydb.ui.page.partial;

import java.io.File;
import java.io.IOException;

import marubinotto.piggydb.model.entity.RawFragment;
import marubinotto.piggydb.ui.page.common.PageImports;
import marubinotto.util.Size;
import net.sf.click.util.ClickUtils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class UploadFile extends AbstractPartial {
	
	public final String jQueryPath = PageImports.JQUERY_PATH;
	
	public String fileName;
	public String fileType;
	public Size fileSize;
	
	public String uploadedFilePath;
	public boolean isImageFile = false;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		if (!getContext().isMultipartRequest()) {
			this.error = "Not a multipart content";
			return;
		}
		
		FileItem fileItem = getContext().getFileItem("file");
		if (fileItem == null) {
			this.error = "The file is missing";
			return;
		}
		
		this.fileName = FilenameUtils.getName(fileItem.getName());
  	this.fileType = RawFragment.getFileType(this.fileName);
  	this.fileSize = new Size(fileItem.getSize());
  	
  	String suffix = this.fileType != null ? ("." + this.fileType) : null;
  	File file = createUploadFilePath(suffix);
  	fileItem.write(file);
  	
  	this.uploadedFilePath = "/" + UPLOAD_DIR_NAME + "/" + file.getName();
  	
  	String mimeType = ClickUtils.getMimeType(this.fileName);
  	if (mimeType != null) {
  		this.isImageFile = mimeType.startsWith("image/");
  	}
	}
	
	private static final String UPLOAD_DIR_NAME = "upload";
	
	private File getUploadDir() throws IOException {
		File dir = new File(getContext().getServletContext().getRealPath("/" + UPLOAD_DIR_NAME));
		if (!dir.isDirectory()) FileUtils.forceMkdir(dir);
		return dir;
	}
	
	private File createUploadFilePath(String suffix) throws IOException {
		String prefix = getContext().getSession().getId();
		return File.createTempFile(prefix, suffix, getUploadDir());
	}
}
