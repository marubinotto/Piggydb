package marubinotto.piggydb.ui.page.partial;

import java.io.File;
import java.io.IOException;

import marubinotto.piggydb.ui.page.common.PageImports;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class UploadFile extends AbstractPartial {
	
	public String fileName;
	public String extension;
	
	public String uploadedFilePath;

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		addModel("jQueryPath", PageImports.JQUERY_PATH);
		
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
  	this.extension = FilenameUtils.getExtension(this.fileName);
  	
  	File file = createUploadFilePath("." + this.extension);
  	fileItem.write(file);
  	
  	this.uploadedFilePath = "/" + UPLOAD_DIR_NAME + "/" + file.getName();
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
