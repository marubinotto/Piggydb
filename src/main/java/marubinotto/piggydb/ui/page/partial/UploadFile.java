package marubinotto.piggydb.ui.page.partial;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import marubinotto.piggydb.ui.page.common.PageImports;

public class UploadFile extends AbstractPartial {

	@Override 
	protected void setModels() throws Exception {
		super.setModels();
		
		addModel("jQueryPath", PageImports.JQUERY_PATH);
		
		getLogger().info(createUploadFilePath(".png"));
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
