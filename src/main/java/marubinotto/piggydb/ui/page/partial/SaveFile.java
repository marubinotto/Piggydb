package marubinotto.piggydb.ui.page.partial;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import marubinotto.util.FileSystemUtils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.UnhandledException;

public class SaveFile extends SaveFragment implements FileItem {

	public String fileName;
	public String uploadedFilePath;
	
	@Override 
	public void bindValues() throws Exception {
		if (isBlank(this.fileName))
			throw new IllegalStateException("fileName is null");
		
		this.fragment.setTitleByUser(this.fileName, getUser());
		this.fragment.setFileInput(this);
	}
	
	private File getUploadedFilePath() {
		if (isBlank(this.uploadedFilePath))
			throw new IllegalStateException("uploadedFilePath is null");
		
		String realPath = getContext().getServletContext()
			.getRealPath(this.uploadedFilePath);
		return new File(realPath);
	}

	@Override
	public String getFieldName() {
		return "file";
	}

	@Override
	public void setFieldName(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return this.fileName;
	}

	@Override
	public boolean isFormField() {
		return false;
	}

	@Override
	public void setFormField(boolean state) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInMemory() {
		return false;
	}

	@Override
	public long getSize() {
		return getUploadedFilePath().length();
	}

	@Override
	public byte[] get() {
		try {
			File uploadedFile = getUploadedFilePath();
			byte[] content = FileUtils.readFileToByteArray(uploadedFile);
			FileSystemUtils.forceDeleteIfExist(uploadedFile);
			return content;
		}
		catch (IOException e) {
			throw new UnhandledException(e);
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return FileUtils.openInputStream(getUploadedFilePath());
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getString() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getString(String encoding) throws UnsupportedEncodingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(File file) throws Exception {
		File uploadedFile = getUploadedFilePath();
		FileUtils.copyFile(uploadedFile, file, true);
		FileSystemUtils.forceDeleteIfExist(uploadedFile);
	}
}
