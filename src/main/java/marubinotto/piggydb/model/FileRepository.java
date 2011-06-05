package marubinotto.piggydb.model;

import java.io.OutputStream;
import java.util.Set;

import marubinotto.util.ZipUtils;

import org.apache.tools.zip.ZipOutputStream;

public interface FileRepository {

	public void putFile(Fragment fragment) throws Exception;
	
	public void getFile(OutputStream output, Fragment fragment)
	throws Exception;
	
	public int size() throws Exception;
	
	public Set<String>getFileNames() throws Exception;
	
	public void outputAll(String namePrefix, ZipOutputStream zipOut)
	throws Exception;
	
	public void clear() throws Exception;
	
	public ZipUtils.EntryReader getEntryReader() throws Exception;
	
	public void deleteFile(Fragment fragment) throws Exception;
}
