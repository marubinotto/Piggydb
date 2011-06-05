package marubinotto.piggydb.fixture.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;

public class FileItemMock implements FileItem {
	
	public byte[] data;
	
	public String name;
	public String fieldName;
	public String contentType;
	
	public boolean isFormField = false;
	
	public FileItemMock(String fieldName, String name, byte[] data) {
		this.fieldName = fieldName;
		this.name = name;
		this.data = data;
	}

	public void delete() {
		throw new UnsupportedOperationException();
	}

	public byte[] get() {
		return this.data;
	}

	public String getContentType() {
		return this.contentType;
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(data);
	}

	public String getName() {
		return this.name;
	}

	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}

	public long getSize() {
		return this.data.length;
	}

	public String getString() {
		return new String(this.data);
	}

	public String getString(String encoding) throws UnsupportedEncodingException {
		return new String(this.data, encoding);
	}

	public boolean isFormField() {
		return this.isFormField;
	}

	public boolean isInMemory() {
		return true;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public void setFormField(boolean isFormField) {
		this.isFormField = isFormField;
	}

	public void write(File file) throws Exception {
		FileUtils.writeByteArrayToFile(file, this.data);
	}
}
